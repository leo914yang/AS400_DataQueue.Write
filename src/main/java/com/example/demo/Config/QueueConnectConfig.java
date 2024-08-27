package com.example.demo.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "queue")
@Data
public class QueueConnectConfig {
    private List<QueueConnectInfoDto> connect = new ArrayList<>();

    public int getTotalQueue() {
        return this.connect.size();
    }
}
