package com.example.demo;


import com.example.demo.Config.QueueConnectInfoDto;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.DataQueueEntry;
import lombok.Getter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class QueueTransferThread extends Thread {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Getter
    private QueueConnectInfoDto connectInfo;
    @Getter
    private String threadName;
    private AS400 hostConnection = null;
    private DataQueue dataQueue = null;
    private final String path;

    public QueueTransferThread(QueueConnectInfoDto info,
                               String path
    ) {
        this.connectInfo = info;
        this.threadName = info.getThreadName();
        this.path = path;
    }

    @Override
    public void run() {
        if (!connectDataQueue()) {
            return;
        }
        try (Stream<String> lines = Files.lines(Path.of(path), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                try {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    dataQueue.write(line);
                } catch (Exception e) {
                    System.out.println("[run] Receive from DataQueue failed: " + e.getMessage());
                    if (!hostConnection.isConnectionAlive()) {
                        closeThread();
                    }
                }
            });

        } catch (Exception e) {
            System.out.println("error: " + e);
        }
        System.out.println(connectInfo.getThreadName() + " 完成");
        closeConnection();
    }

    private boolean connectDataQueue() {
        closeConnection();

        boolean result = false;
        int currentRetryTime = 0;
            while (currentRetryTime <= connectInfo.getMaxRetryCount()) {
                try {
                    hostConnection = new AS400(connectInfo.getHost(), connectInfo.getUser(), connectInfo.getPassword());
                    dataQueue = new DataQueue(hostConnection, connectInfo.getQueuePath());
                    hostConnection.connectService(AS400.DATAQUEUE);
                    result = true;
                    break;
                } catch (Exception e) {
                    currentRetryTime++;
                    System.out.println("[connectDataQueue] Connect DataQueue failed: " + e.getMessage());

                }

                try {
                    Thread.sleep(3 * 1000L);
                } catch (InterruptedException e) {
                    System.out.println("[connectDataQueue] Thread.sleep failed: " + e.getMessage());

                }
            }

        return result;
    }

    private void closeConnection() {
        if (hostConnection != null) {
            try {
                hostConnection.disconnectService(AS400.DATAQUEUE);
            } catch (Exception e) {
                System.out.println("[closeDataQueue] Close DataQueue failed: " + e.getMessage());

            } finally {
                hostConnection = null;
            }
        }
    }

    public void closeThread() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // 等待所有任務完成，最長等待時間為1分鐘
                if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    // 如果等待超時，則強制終止所有任務
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        closeConnection();
    }

}

