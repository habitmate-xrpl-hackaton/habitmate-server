package com.example.xrpl.xrpl.api;

public interface XRPLTestWalletService {
    CreateWalletResponse createWallet();

    record CreateWalletResponse(
            String address,
            String secret
    ) {
    }
}
