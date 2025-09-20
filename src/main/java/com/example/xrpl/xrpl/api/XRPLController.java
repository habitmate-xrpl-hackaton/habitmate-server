package com.example.xrpl.xrpl.api;

import com.example.xrpl.xrpl.application.XRPLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/xrpl")
@RequiredArgsConstructor
public class XRPLController {

    private final XRPLService xrplService;
    private final XRPLTestWalletService xrplTestWalletService;

    @PostMapping("/escrow/complete/batch")
    public ResponseEntity<Map<String, String>> completeBatchEscrow(
            @RequestBody BatchEscrowRequest request
    ) {
        try {
            xrplService.completeBatchEscrow(request.escrows());
            return ResponseEntity.ok(Map.of(
                    "message", "Batch escrow completion submitted successfully.",
                    "totalEscrows", String.valueOf(request.escrows().size()),
                    "type", "batchEscrow"
            ));
        } catch (Exception e) {
            log.error("Failed to complete batch escrow", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to complete batch escrow: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/payment/batch")
    public ResponseEntity<Map<String, String>> sendBatchPayment(
            @RequestBody BatchPaymentRequest request
    ) {
        try {
            xrplService.sendBatchPayment(request.payments());
            return ResponseEntity.ok(Map.of(
                    "message", "Batch payment submitted successfully.",
                    "totalPayments", String.valueOf(request.payments().size()),
                    "type", "standardBatch"
            ));
        } catch (Exception e) {
            log.error("Failed to send batch payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to send batch payment: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/nft/mint")
    public ResponseEntity<Map<String, String>> mintNFT(
            @RequestParam String recipientAddress,
            @RequestParam(required = false) String uri
    ) {
        try {
            String payloadUuid = xrplService.mintNFT(recipientAddress, uri);
            return ResponseEntity.ok(Map.of(
                    "payloadUuid", payloadUuid,
                    "message", "NFT mint payload created successfully. Please sign using XUMM app."
            ));
        } catch (Exception e) {
            log.error("Failed to mint NFT", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to mint NFT: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/nft/{source}/uris")
    public ResponseEntity<List<String>> getNftUri(@PathVariable String source) {
        try {
            List<String> uris = xrplService.nftUris(source);
            return ResponseEntity.ok(uris);
        } catch (Exception e) {
            log.error("Failed to get payload status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/nft/issue")
    public ResponseEntity<Map<String, String>> issueNFT(
            @RequestParam(required = false) String dest,
            @RequestParam(required = false) String uri
    ) {
        try {
            String transactionHash = xrplService.mintNFT(dest, uri);
            return ResponseEntity.ok(Map.of(
                    "transactionHash", transactionHash,
                    "message", "NFT issued successfully to blockchain",
                    "uri", uri != null ? uri : "No URI provided"
            ));
        } catch (Exception e) {
            log.error("Failed to issue NFT", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to issue NFT: " + e.getMessage()
            ));
        }
    }

    /**
     * Request DTO for batch payment
     */
    public record BatchPaymentRequest(
            List<XRPLService.PaymentParams> payments
    ) {
    }

    /**
     * Request DTO for batch escrow completion
     */
    public record BatchEscrowRequest(
            List<XRPLService.EscrowParams> escrows
    ) {
    }

//    @PostMapping("/test-create-wallet")
//    public ResponseEntity<XRPLTestWalletService.CreateWalletResponse> createTestWallet() {
//        return ResponseEntity.ok(xrplTestWalletService.createWallet());
//    }
}