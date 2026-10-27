package com.ecom.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "uptime", uptime,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
