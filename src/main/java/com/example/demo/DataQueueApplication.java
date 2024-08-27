package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DataQueueApplication {
	@Autowired
	private ThreadManager threadManager;
	public static void main(String[] args) {
		SpringApplication.run(DataQueueApplication.class, args);
	}
	@EventListener(ApplicationReadyEvent.class)
	public void startThreadWork() {
		threadManager.initAndFireAllThread();
	}
}
