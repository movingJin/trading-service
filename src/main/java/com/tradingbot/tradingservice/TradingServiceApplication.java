package com.tradingbot.tradingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.config.EnableWebFlux;


@EnableAspectJAutoProxy
@SpringBootApplication
@EnableWebFlux
public class TradingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingServiceApplication.class, args);
	}

}
