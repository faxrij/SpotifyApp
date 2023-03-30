package com.example.spotifyproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpotifyProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyProjectApplication.class, args);
    }

}