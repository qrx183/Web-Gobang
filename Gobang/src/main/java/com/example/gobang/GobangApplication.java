package com.example.gobang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class GobangApplication {
    public static ConfigurableApplicationContext context;
    public static void main(String[] args) {

        context = SpringApplication.run(GobangApplication.class, args);
    }

}
