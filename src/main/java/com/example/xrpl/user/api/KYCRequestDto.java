package com.example.xrpl.user.api;

import lombok.Getter;

@Getter
public class KYCRequestDto {
    private String walletAddress;
    private boolean isKYC;
}