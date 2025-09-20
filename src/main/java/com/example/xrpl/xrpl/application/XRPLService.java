package com.example.xrpl.xrpl.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.math.BigDecimal;
import java.util.List;

public interface XRPLService {

    String mintNFT(String dest, String uri);
    List<String> nftUris(String source);
    
    /**
     * Creates an escrow transaction using XUMM API and returns QR + hash info
     * @param source The source address for the escrow
     * @param amount The amount to escrow in XRP
     * @param memo Optional memo for the transaction
     * @param finishAfter Optional finish after timestamp
     * @param cancelAfter Optional cancel after timestamp
     * @param condition Optional condition for the escrow
     * @return Escrow creation response with QR and payload info
     */
    EscrowCreateResponse createEscrowWithXumm(String source, BigDecimal amount, String memo, Long finishAfter, Long cancelAfter, String condition);
    
    /**
     * Completes an escrow transaction using XUMM
     * @param escrowOwner The owner of the escrow
     * @param offerSequence The sequence number of the escrow offer
     * @return XUMM payload UUID that can be used to track the transaction
     */
    String completeEscrow(String escrowOwner, Integer offerSequence) throws JsonRpcClientErrorException, JsonProcessingException;

    void sendBatchPayment(List<PaymentParams> payments);

    /**
     * Gets the status of a XUMM payload
     * @param payloadUuid The UUID of the XUMM payload
     * @return Status information about the payload
     */
    PayloadStatus getPayloadStatus(String payloadUuid);
    
    /**
     * Gets account information from XRPL
     * @param address The XRPL address to get information for
     * @return Account information including balance, sequence, etc.
     */
    AccountInfoResult getAccountInfo(XrplClient xrplClient, Address address);
    
    /**
     * Represents the status of a XUMM payload
     */
    record PayloadStatus(
        String uuid,
        String status, // WAITING, OPENED, SIGNED, CANCELLED, EXPIRED
        String transactionHash,
        String signerAddress,
        boolean signed
    ) {}

    record PaymentParams(
        String destinationAddress,
        Long destinationTag,
        BigDecimal amount,
        String memo
    ) {}

    /**
     * Response for escrow creation with XUMM
     */
    record EscrowCreateResponse(
        String payloadUuid,
        String qrPngUrl,
        String signUrl,
        String webhookUrl,
        String returnUrl,
        String message
    ) {}
}