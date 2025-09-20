package com.example.xrpl.xrpl.api;

import com.example.xrpl.xrpl.application.XRPLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/xrpl")
@RequiredArgsConstructor
public class XRPLController {

    private final XRPLService xrplService;

    @PostMapping("/escrow/complete")
    public ResponseEntity<Map<String, String>> completeEscrow(
            @RequestParam String escrowOwner,
            @RequestParam Integer offerSequence) {
        try {
            String payloadUuid = xrplService.completeEscrow(escrowOwner, offerSequence);
            return ResponseEntity.ok(Map.of(
                "payloadUuid", payloadUuid,
                "message", "Escrow completion payload created successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to complete escrow", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to complete escrow: " + e.getMessage()
            ));
        }
    }

//    @PostMapping("/payment/batch")
//    public ResponseEntity<Map<String, String>> sendBatchPayment(
//            @RequestBody BatchPaymentRequest request) {
//        try {
//            String payloadUuid = xrplService.sendBatchPayment(request.payments());
//            return ResponseEntity.ok(Map.of(
//                "payloadUuid", payloadUuid,
//                "message", "Batch payment payload created successfully. Please sign using XUMM app.",
//                "totalPayments", String.valueOf(request.payments().size())
//            ));
//        } catch (Exception e) {
//            log.error("Failed to send batch payment", e);
//            return ResponseEntity.badRequest().body(Map.of(
//                "error", "Failed to send batch payment: " + e.getMessage()
//            ));
//        }
//    }

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

    @GetMapping("/payload/{uuid}/status")
    public ResponseEntity<XRPLService.PayloadStatus> getPayloadStatus(@PathVariable String uuid) {
        try {
            XRPLService.PayloadStatus status = xrplService.getPayloadStatus(uuid);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get payload status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received XUMM webhook: {}", payload);
        
        // Here you can implement webhook handling logic
        // For example, update database when a transaction is signed
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleReturn(@RequestParam(required = false) String uuid) {
        log.info("User returned from XUMM with payload UUID: {}", uuid);
        
        if (uuid != null) {
            try {
                XRPLService.PayloadStatus status = xrplService.getPayloadStatus(uuid);
                return ResponseEntity.ok(String.format(
                    "Transaction %s. Status: %s", 
                    status.signed() ? "completed successfully" : "is pending",
                    status.status()
                ));
            } catch (Exception e) {
                log.error("Failed to get payload status for return", e);
                return ResponseEntity.ok("Unable to determine transaction status");
            }
        }
        
        return ResponseEntity.ok("Returned from XUMM");
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

    @PostMapping("/escrow/create-with-xumm")
    public ResponseEntity<XRPLService.EscrowCreateResponse> createEscrowWithXumm(
            @RequestParam String sourceAddress,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) Long finishAfter,
            @RequestParam(required = false) Long cancelAfter,
            @RequestParam(required = false) String condition) {
        try {
            XRPLService.EscrowCreateResponse response = xrplService.createEscrowWithXumm(
                sourceAddress, amount, memo, finishAfter, cancelAfter, condition
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create escrow with XUMM", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Request DTO for batch payment
     */
    public record BatchPaymentRequest(
        List<XRPLService.PaymentParams> payments
    ) {}

    @PostMapping("/test-create-wallet")
    public ResponseEntity<XRPLService.CreateWalletResponse> createTestWallet() {
        return ResponseEntity.ok(xrplService.createWallet());
    }
}