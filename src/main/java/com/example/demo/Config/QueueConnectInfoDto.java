package com.example.demo.Config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueueConnectInfoDto {

    private String threadName;

    private String host;

    private String user;

    private String password;

    private String queuePath;

    private String dataGroup;

    private int maxWaitTime;

    private int maxRetryCount;

    private int maxIdleHeartbeat;

    private boolean ackMode;

    private int messageProcessorThreadCount;

    private String activeQueue;
}
