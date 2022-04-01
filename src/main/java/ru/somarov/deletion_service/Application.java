package ru.somarov.deletion_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.somarov")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
