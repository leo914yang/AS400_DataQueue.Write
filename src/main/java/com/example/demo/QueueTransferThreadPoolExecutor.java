package com.example.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueueTransferThreadPoolExecutor extends ThreadPoolExecutor {

    private ThreadManager threadManager;

    public QueueTransferThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadManager threadManager) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.threadManager = threadManager;
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable e) {
        super.afterExecute(runnable, e);

        if (runnable.getClass() == QueueTransferThread.class) {
            QueueTransferThread workerThread = (QueueTransferThread) runnable;
            String threadName = workerThread.getThreadName();
            workerThread.closeThread();
        }
    }
}
