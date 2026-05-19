package com.example.deustobank;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableScheduling
@SpringBootApplication
public class DeustoBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeustoBankApplication.class, args);
	}

}
