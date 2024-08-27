package com.example.demo;

import com.example.demo.Config.QueueConnectConfig;
import com.example.demo.Config.QueueConnectInfoDto;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThreadManager {
    private QueueTransferThreadPoolExecutor workerThreadPoolExecutor;
    @Autowired
    private QueueConnectConfig connectConfig;

    @PostConstruct
    private void initThreadManager() {
        int totalQueue = connectConfig.getTotalQueue();
        workerThreadPoolExecutor = new QueueTransferThreadPoolExecutor(totalQueue, totalQueue, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), this);
    }

    public void initAndFireAllThread() {
        int totalQueue = connectConfig.getTotalQueue();
        List<QueueConnectInfoDto> queueConnectInfos = connectConfig.getConnect();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter directory path: ");
        Path directoryPath = Paths.get(scanner.nextLine());
        List<File> txtFiles = getTxtFilesInDirectory(directoryPath);
        try {
            for (File path : txtFiles) {
                String fileNameWithoutExtension = getFileNameWithoutExtension(path.getName());

                QueueConnectInfoDto matchingInfo = queueConnectInfos.stream()
                        .filter(info -> info.getThreadName().equals(fileNameWithoutExtension))
                        .findFirst()
                        .orElse(null);

                if (matchingInfo != null) {
                    QueueTransferThread workerThread = new QueueTransferThread(matchingInfo, path.toString());
                    workerThreadPoolExecutor.execute(workerThread);
                }
            }
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    private List<File> getTxtFilesInDirectory(Path directory) {
        try {
            return Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return List.of();
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName; // No extension found
        }
        return fileName.substring(0, lastDotIndex);
    }

}
