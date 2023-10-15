package com.kabgig.CoinBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoinBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(CoinBotApplication.class, args);
	}
}
