package com.example.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс запуска Spring Boot приложения для блога.
 * 
 * При запуске инициализирует Spring-контекст и поднимает встроенный сервер.
 */
@SpringBootApplication
public class BlogApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки (необязательные)
     */
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}