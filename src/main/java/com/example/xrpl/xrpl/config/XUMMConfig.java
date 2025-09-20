package com.example.xrpl.xrpl.config;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "xumm")
public class XUMMConfig {

    private String mainAddress;
    private String apiKey;
    private String apiSecret;
    private String baseUrl = "https://xumm.app/api/v1/platform";
    private String webhookUrl;
    private String returnUrl;
    private Long expirationMinutes = 5L;
    private String xrplRpcUrl = "https://s1.ripple.com:51234"; // XRPL Mainnet RPC endpoint

    @Bean
    public WebClient xummWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("X-API-Secret", apiSecret)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    @Bean
    public WebClient xrplRpcWebClient() {
        return WebClient.builder()
                .baseUrl(xrplRpcUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}