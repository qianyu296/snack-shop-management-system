package com.snackadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SnackAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnackAdminApplication.class, args);
    }
}
