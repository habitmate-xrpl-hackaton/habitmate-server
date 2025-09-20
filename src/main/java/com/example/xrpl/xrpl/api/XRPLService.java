package com.example.xrpl.xrpl.api;

import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.math.BigDecimal;
import java.util.List;

public interface XRPLService {

    String mintNFT(String dest, String uri);

    List<String> nftUris(String source);
    /**
     * Completes multiple escrow transactions at once (batch processing)
     * 
     * @param escrows List of escrow parameters to complete
     */
    void completeBatchEscrow(List<EscrowParams> escrows);

    /**
     * Creates a credential for a subject
     * 
     * @param credentialParams Parameters for credential creation
     * @return Transaction hash and credential details
     */
    CredentialCreateResponse createCredential(CredentialCreateParams credentialParams);

    /**
     * Accepts a credential as the subject
     * 
     * @param credentialAcceptParams Parameters for credential acceptance
     * @return Transaction hash and acceptance confirmation
     */
    CredentialAcceptResponse acceptCredential(CredentialAcceptParams credentialAcceptParams);

    void sendBatchPayment(List<PaymentParams> payments);

    /**
     * Gets account information from XRPL
     *
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
    ) {
    }

    record PaymentParams(
            String destinationAddress,
            Long destinationTag,
            BigDecimal amount,
            String memo
    ) {
    }

    /**
     * Parameters for escrow completion
     */
    record EscrowParams(
            String escrowOwner,
            Integer offerSequence
    ) {
    }

    /**
     * Parameters for credential creation
     */
    record CredentialCreateParams(
            String subjectAddress,
            String credentialType,
            String uri,
            Long expirationDays  // Days from now until expiration
    ) {
    }

    /**
     * Parameters for credential acceptance
     */
    record CredentialAcceptParams(
            String issuerAddress,
            String credentialType
    ) {
    }

    /**
     * Response for credential creation
     */
    record CredentialCreateResponse(
            String transactionHash,
            String issuerAddress,
            String subjectAddress,
            String credentialType,
            String uri,
            String expirationDate,
            String message
    ) {
    }

    /**
     * Response for credential acceptance
     */
    record CredentialAcceptResponse(
            String transactionHash,
            String subjectAddress,
            String issuerAddress,
            String credentialType,
            String message
    ) {
    }
}