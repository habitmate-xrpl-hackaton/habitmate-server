package com.example.xrpl.xrpl.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "xrpl")
public class XRPLConfig {

    private String rpcUrl;
    private String centralWalletAddress;
    private String centralWalletSecret;
    private String networkId;
    private Long pollingIntervalMs = 30000L; // 30 seconds

    private String nftIssuerAddress;
    private String nftIssuerSecret;
    private String nftUriPrefix = "https://example.com/metadata/";

    private KeyPair centralWalletKeyPair;
    private KeyPair nftIssuerKeyPair;

    private XrplClient xrplClient;

    @PostConstruct
    public void initialize() {
        try {
            this.xrplClient = new XrplClient(HttpUrl.get(rpcUrl));

            this.centralWalletKeyPair = Seed
                    .fromBase58EncodedSecret(Base58EncodedSecret.of(centralWalletSecret))
                    .deriveKeyPair();
            this.nftIssuerKeyPair = Seed
                    .fromBase58EncodedSecret(Base58EncodedSecret.of(nftIssuerSecret))
                    .deriveKeyPair();

            log.info("XRPL4J Service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize XRPL4J Service", e);
            throw new RuntimeException("XRPL4J initialization failed", e);
        }
    }
}
