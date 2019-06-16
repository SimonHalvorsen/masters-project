package com.mastersproject.alarmservice;

import com.mastersproject.alarmservice.kafkaConsumer.Consumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AlarmServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlarmServiceApplication.class, args);
			Consumer consumer = new Consumer();
			consumer.run();
	}

}
