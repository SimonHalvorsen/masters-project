package com.mastersproject.vveurekanamingserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class VvEurekaNamingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VvEurekaNamingServerApplication.class, args);
	}

}
