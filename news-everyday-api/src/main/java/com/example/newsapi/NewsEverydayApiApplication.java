package com.example.newsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class NewsEverydayApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsEverydayApiApplication.class, args);
    }

}
