package com.nyctrees.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the NYC Trees backend application.
 */
@SpringBootApplication
public class NycTreesApplication {

    /**
     * Starts the Spring application context.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(NycTreesApplication.class, args);
    }
}
