package com.snackadmin.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "snack-admin-jwt-secret-key-2026-must-be-at-least-256-bits-long";
    private long expiration = 86400000; // 24 hours
}
