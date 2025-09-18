package com.example.xrpl.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CommonController {

    @GetMapping("/health_check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("up");
    }
}
