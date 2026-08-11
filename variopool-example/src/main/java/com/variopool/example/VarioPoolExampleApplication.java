package com.variopool.example;

import com.variopool.spring.annotation.EnableVarioPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableVarioPool
public class VarioPoolExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(VarioPoolExampleApplication.class, args);
    }
}
