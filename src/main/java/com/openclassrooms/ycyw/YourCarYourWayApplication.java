package com.openclassrooms.ycyw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YourCarYourWayApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourCarYourWayApplication.class, args);
    }
}
