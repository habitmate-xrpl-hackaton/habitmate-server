package com.example.xrpl.xrpl.api;

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

    @PostMapping("/credential/create")
    public ResponseEntity<XRPLService.CredentialCreateResponse> createCredential(
            @RequestBody CredentialCreateRequest request
    ) {
        try {
            XRPLService.CredentialCreateResponse response = xrplService.createCredential(
                    new XRPLService.CredentialCreateParams(
                            request.subjectAddress(),
                            request.credentialType(),
                            request.uri(),
                            request.expirationDays()
                    )
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create credential", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/credential/accept")
    public ResponseEntity<XRPLService.CredentialAcceptResponse> acceptCredential(
            @RequestBody CredentialAcceptRequest request
    ) {
        try {
            XRPLService.CredentialAcceptResponse response = xrplService.acceptCredential(
                    new XRPLService.CredentialAcceptParams(
                            request.issuerAddress(),
                            request.credentialType()
                    )
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to accept credential", e);
            return ResponseEntity.badRequest().build();
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

    /**
     * Request DTO for credential creation
     */
    public record CredentialCreateRequest(
            String subjectAddress,
            String credentialType,
            String uri,
            Long expirationDays
    ) {
    }

    /**
     * Request DTO for credential acceptance
     */
    public record CredentialAcceptRequest(
            String issuerAddress,
            String credentialType
    ) {
    }
}