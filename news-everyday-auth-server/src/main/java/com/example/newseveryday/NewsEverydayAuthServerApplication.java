package com.example.newseveryday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class NewsEverydayAuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsEverydayAuthServerApplication.class, args);
    }
}
