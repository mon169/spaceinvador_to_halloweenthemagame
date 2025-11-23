package com.game.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameApplication.class, args);
        System.out.println("🚀 Spring Boot Game Server Running on 8080 (PostgreSQL)");
    }
}