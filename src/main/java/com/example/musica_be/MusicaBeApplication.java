package com.example.musica_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MusicaBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicaBeApplication.class, args);
    }

}
