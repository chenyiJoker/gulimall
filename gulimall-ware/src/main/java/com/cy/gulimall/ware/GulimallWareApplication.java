package com.cy.gulimall.ware;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableRabbit
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@ComponentScan("com.cy")
@EnableFeignClients
public class GulimallWareApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallWareApplication.class, args);
	}

}
