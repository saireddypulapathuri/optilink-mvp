package com.optilink.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot adapter for OptiLink operational APIs.
 */
@SpringBootApplication
public class OptiLinkServerApplication {

    /**
     * Starts the server.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OptiLinkServerApplication.class, args);
    }
}
