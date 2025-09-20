package com.example.xrpl.xrpl.application;

import com.example.xrpl.xrpl.config.XRPLConfig;
import com.example.xrpl.xrpl.config.XUMMConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.NfTokenCreateOfferFlags;
import org.xrpl.xrpl4j.model.transactions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class XRPLServiceImpl implements XRPLService {

    private final XRPLConfig xrplConfig;
    private final XUMMConfig xummConfig;
    private final WebClient xummWebClient;
    private final WebClient xrplRpcWebClient;
    private final ObjectMapper objectMapper;

    private static final long RIPPLE_OFFSET = 946684800; // Ripple 시간 오프셋 (UNIX -> Ripple 변환에 사용)
    private static final long MAX_RIPPLE_TIME = 0xFFFFFFFFL; // Ripple 시간이 담기는 필드는 32비트 부호 없는 정수
    private static final UnsignedInteger ESCROW_D_TAG = UnsignedInteger.ONE;

    public CreateWalletResponse createWallet() {
        final HttpUrl devnetUrl = HttpUrl.parse(xrplConfig.getRpcUrl());
        final HttpUrl faucetUrl = HttpUrl.parse("https://faucet.devnet.rippletest.net");
        assert devnetUrl != null;
        final FaucetClient faucetClient = FaucetClient.construct(faucetUrl);
        final Seed seed = Seed.ed25519Seed();
        final KeyPair randomKeyPair = seed.deriveKeyPair();

        faucetClient.fundAccount(FundAccountRequest.of(randomKeyPair.publicKey().deriveAddress()));

        return new CreateWalletResponse(
                randomKeyPair.publicKey().deriveAddress().value(),
                seed.decodedSeed().bytes().toString() // TODO : to string
        );
    }

    @Override
    public EscrowCreateResponse createEscrowWithXumm(String source, BigDecimal amount, String memo, Long finishAfter, Long cancelAfter, String condition) {
        try {
            log.info("Creating escrow with XUMM: source={}, amount={}, memo={}", source, amount, memo);

            Map<String, Object> txJson = new HashMap<>();
            txJson.put("TransactionType", "EscrowCreate");
//            txJson.put("Account", source);
            txJson.put("Destination", xummConfig.getMainAddress()); // Use main address as destination
            txJson.put("Amount", String.valueOf(xrpToDrops(amount)));
            txJson.put("Fee", "12");

            // Add optional escrow parameters
            if (finishAfter != null) {
                txJson.put("FinishAfter", getSafeFinishAfter(finishAfter));
            }
            if (cancelAfter != null) {
                txJson.put("CancelAfter", cancelAfter);
            }
            if (condition != null && !condition.trim().isEmpty()) {
                txJson.put("Condition", condition);
            }

            // Add memo if provided
            if (memo != null && !memo.trim().isEmpty()) {
                txJson.put("Memos", createMemoArray(memo));
            }

            log.info("EscrowCreate transaction JSON: {}", txJson);

            // Create XUMM payload with enhanced response
            Map<String, Object> payload = new HashMap<>();
            payload.put("txjson", txJson);

            Map<String, Object> options = new HashMap<>();
            options.put("submit", true);
            options.put("multisign", false);
            options.put("expire", xummConfig.getExpirationMinutes());
            payload.put("options", options);

            Map<String, Object> customMeta = new HashMap<>();
            customMeta.put("identifier", "escrow-create-" + System.currentTimeMillis());
            customMeta.put("blob", Map.of(
                    "title", "Create Escrow",
                    "instruction", "Sign to create an escrow with " + amount + " XRP"
            ));

            if (xummConfig.getWebhookUrl() != null) {
                customMeta.put("instruction", xummConfig.getWebhookUrl());
            }

            if (xummConfig.getReturnUrl() != null) {
                customMeta.put("return_url", Map.of(
                        "web", xummConfig.getReturnUrl(),
                        "app", xummConfig.getReturnUrl()
                ));
            }

            payload.put("custom_meta", customMeta);

            // Submit to XUMM
            JsonNode response = xummWebClient
                    .post()
                    .uri("/payload")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("uuid")) {
                throw new RuntimeException("Invalid response from XUMM API");
            }

            String uuid = response.get("uuid").asText();
            String qrPng = response.get("refs").get("qr_png").asText();
            String signUrl = response.get("next").get("always").asText();

            log.info("Created XUMM escrow payload: UUID={}, QR={}, SignURL={}", uuid, qrPng, signUrl);

            return new EscrowCreateResponse(
                    uuid,
                    qrPng,
                    signUrl,
                    xummConfig.getWebhookUrl(),
                    xummConfig.getReturnUrl(),
                    "Escrow creation payload created successfully. Please sign using XUMM app."
            );

        } catch (WebClientResponseException e) {
            log.error("Failed to create XUMM escrow payload: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to create XUMM escrow payload", e);
        } catch (Exception e) {
            log.error("Failed to create escrow with XUMM", e);
            throw new RuntimeException("Failed to create escrow with XUMM", e);
        }
    }

    @Override
    public String mintNFT(String dest, String uri) {
        try {
            final XrplClient xrplClient = xrplConfig.getXrplClient();
            final KeyPair mainWalletKeyPair = xrplConfig.getCentralWalletKeyPair();
            final AccountInfoResult mainAccountInfo = getAccountInfo(xrplClient, mainWalletKeyPair.publicKey().deriveAddress());
            final FeeResult feeResult = xrplClient.fee();

            final NfTokenUri nfTokenUri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");
            final NfTokenMint nfTokenMint = NfTokenMint.builder()
                    .tokenTaxon(UnsignedLong.ONE)
                    .account(mainWalletKeyPair.publicKey().deriveAddress())
                    .signingPublicKey(mainWalletKeyPair.publicKey())
                    .sequence(mainAccountInfo.accountData().sequence())
                    .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
                    .uri(nfTokenUri)
                    .build();

            final BcSignatureService signatureService = new BcSignatureService();
            final SingleSignedTransaction<NfTokenMint> transaction = signatureService.sign(mainWalletKeyPair.privateKey(), nfTokenMint);
            final SubmitResult<NfTokenMint> result = xrplClient.submit(transaction);
//
//            final NfTokenCreateOffer offer = NfTokenCreateOffer.builder()
//                    .account(mainWalletKeyPair.publicKey().deriveAddress())
//                    .nftokenId(nftokenId)
//                    .amount(XrpCurrencyAmount.ofDrops(0)) // 0이면 무료 전송
//                    .destination(Address.of(destinationAddress))
//                    .signingPublicKey(mainWalletKeyPair.publicKey())
//                    .sequence(mainAccountInfo.accountData().sequence())
//                    .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
//                    .build();
//
//            // send NFT
//            final NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
//                    .account(keyPair.publicKey().deriveAddress())
//                    .nfTokenId(tokenId)
//                    .fee(FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee())
//                    .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
//                    .amount(XrpCurrencyAmount.ofDrops(1000))
//                    .flags(NfTokenCreateOfferFlags.builder()
//                            .tfSellToken(true)
//                            .build())
//                    .signingPublicKey(keyPair.publicKey())
//                    .build();
//
//            SingleSignedTransaction<NfTokenCreateOffer> signedOffer = signatureService.sign(
//                    keyPair.privateKey(),
//                    nfTokenCreateOffer
//            );
//            SubmitResult<NfTokenCreateOffer> nfTokenCreateOfferSubmitResult = xrplClient.submit(signedOffer);
//            assertThat(nfTokenCreateOfferSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
//            assertThat(signedOffer.hash()).isEqualTo(nfTokenCreateOfferSubmitResult.transactionResult().hash());

            if (result.engineResult().equals("tesSUCCESS")) {
                log.info("Issue NFT finish result, HASH: {} {}", result.engineResult(), result.transactionResult().hash());
                return result.engineResultMessage();
            } else {
                log.error("Issue NFT finish result: {}", result.engineResult());
                throw new RuntimeException("Issue NFT finish failed");
            }
        } catch (Exception e) {
            log.error("Failed to issue NFT", e);
            throw new RuntimeException("Failed to issue NFT", e);
        }
    }

    @Override
    public List<String> nftUris(String source) {
        try {
            final XrplClient xrplClient = xrplConfig.getXrplClient();
            final AccountNftsResult accountNftsResult = xrplClient.accountNfts(Address.of(source));
            return accountNftsResult.accountNfts().stream().map(nfTokenObject -> {
                if (nfTokenObject.uri().isEmpty()) {
                    throw new RuntimeException("NFT URI is empty");
                }
                final String hex = nfTokenObject.uri().get().value();
                byte[] bytes = DatatypeConverter.parseHexBinary(hex);
                return new String(bytes);
            }).toList();
        } catch (Exception e) {
            log.error("Failed to get NFT", e);
            throw new RuntimeException("Failed to get NFT", e);
        }
    }

    @Override
    public String completeEscrow(String escrowOwner, Integer offerSequence)
            throws JsonRpcClientErrorException, JsonProcessingException {
        final XrplClient xrplClient = xrplConfig.getXrplClient();
        final KeyPair mainWalletKeyPair = xrplConfig.getCentralWalletKeyPair();
        final AccountInfoResult mainAccountInfo = getAccountInfo(xrplClient, mainWalletKeyPair.publicKey().deriveAddress());
        final FeeResult feeResult = xrplClient.fee();

        final EscrowFinish escrowFinish = EscrowFinish.builder()
                .account(mainWalletKeyPair.publicKey().deriveAddress())
                .fee(feeResult.drops().openLedgerFee())
                .sequence(mainAccountInfo.accountData().sequence())
                .owner(Address.of(escrowOwner))
                .offerSequence(UnsignedInteger.valueOf(offerSequence))
                .signingPublicKey(mainWalletKeyPair.publicKey())
                .build();

        final BcSignatureService signatureService = new BcSignatureService();
        final SingleSignedTransaction<EscrowFinish> transaction = signatureService.sign(
                mainWalletKeyPair.privateKey(), escrowFinish
        );
        final SubmitResult<EscrowFinish> result = xrplClient.submit(transaction);

        if (result.engineResult().equals("tesSUCCESS")) {
            log.info("Escrow finish result, HASH: {} {}", result.engineResult(), result.transactionResult().hash());
            return result.engineResultMessage();
        } else {
            log.error("Escrow finish result: {}", result.engineResult());
            throw new RuntimeException("Escrow finish failed");
        }
    }

    @Override
    public void sendBatchPayment(List<PaymentParams> payments) {
        try {
            final XrplClient xrplClient = xrplConfig.getXrplClient();
            final KeyPair mainWalletKeyPair = xrplConfig.getCentralWalletKeyPair();
            final AccountInfoResult mainAccountInfo = getAccountInfo(xrplClient, mainWalletKeyPair.publicKey().deriveAddress());
            final FeeResult feeResult = xrplClient.fee();

//            FeeResult feeResult = xrplClient.fee();
//            AccountInfoResult accountInfo = this.scanForResult(
//                    () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
//            );
//            XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
//
//            Transaction transaction = Transaction
//
//            Payment payment = Payment.builder()
//                    .account(sourceKeyPair.publicKey().deriveAddress())
//                    .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
//                    .sequence(accountInfo.accountData().sequence())
//                    .destination(destinationKeyPair.publicKey().deriveAddress())
//                    .amount(amount)
//                    .signingPublicKey(sourceKeyPair.publicKey())
//                    .build();
//
//            SingleSignedTransaction<Payment> signedPayment = signatureService.sign(sourceKeyPair.privateKey(), payment);
//            SubmitResult<Payment> result = xrplClient.submit(signedPayment);

//            log.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

        } catch (Exception e) {
            log.error("Failed to send payment", e);
            throw new RuntimeException("Failed to send payment", e);
        }
    }

    @Override
    public PayloadStatus getPayloadStatus(String payloadUuid) {
        try {
            JsonNode response = xummWebClient
                    .get()
                    .uri("/payload/" + payloadUuid)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from XUMM API");
            }

            JsonNode meta = response.get("meta");
            JsonNode response_node = response.get("response");

            String status = meta.get("signed").asBoolean() ? "SIGNED" :
                    meta.get("expired").asBoolean() ? "EXPIRED" :
                            meta.get("cancelled").asBoolean() ? "CANCELLED" :
                                    response_node != null && response_node.get("opened").asBoolean() ? "OPENED" : "WAITING";

            String transactionHash = null;
            String signerAddress = null;

            if (response_node != null && response_node.get("txid") != null) {
                transactionHash = response_node.get("txid").asText();
            }

            if (response_node != null && response_node.get("account") != null) {
                signerAddress = response_node.get("account").asText();
            }

            return new PayloadStatus(
                    payloadUuid,
                    status,
                    transactionHash,
                    signerAddress,
                    meta.get("signed").asBoolean()
            );
        } catch (WebClientResponseException e) {
            log.error("Failed to get payload status: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to get payload status", e);
        } catch (Exception e) {
            log.error("Failed to get payload status", e);
            throw new RuntimeException("Failed to get payload status", e);
        }
    }

    private String createPayload(Map<String, Object> txJson, String title, String instruction) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("txjson", txJson);

            Map<String, Object> options = new HashMap<>();
            options.put("submit", true);
            options.put("multisign", false);
            options.put("expire", xummConfig.getExpirationMinutes());
            payload.put("options", options);

            Map<String, Object> customMeta = new HashMap<>();
            customMeta.put("identifier", "xrpl-service-" + System.currentTimeMillis());
            customMeta.put("blob", Map.of(
                    "title", title,
                    "instruction", instruction
            ));

            if (xummConfig.getWebhookUrl() != null) {
                customMeta.put("instruction", xummConfig.getWebhookUrl());
            }

            if (xummConfig.getReturnUrl() != null) {
                customMeta.put("return_url", Map.of(
                        "web", xummConfig.getReturnUrl(),
                        "app", xummConfig.getReturnUrl()
                ));
            }

            payload.put("custom_meta", customMeta);

            JsonNode response = xummWebClient
                    .post()
                    .uri("/payload")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("uuid")) {
                throw new RuntimeException("Invalid response from XUMM API");
            }

            String uuid = response.get("uuid").asText();
            String qrPng = response.get("refs").get("qr_png").asText();
            String signUrl = response.get("next").get("always").asText();

            log.info("Created XUMM payload: UUID={}, QR={}, SignURL={}", uuid, qrPng, signUrl);

            return uuid;
        } catch (WebClientResponseException e) {
            log.error("Failed to create XUMM payload: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to create XUMM payload", e);
        } catch (Exception e) {
            log.error("Failed to create XUMM payload", e);
            throw new RuntimeException("Failed to create XUMM payload", e);
        }
    }

    private long xrpToDrops(BigDecimal xrp) {
        BigDecimal drops = xrp.multiply(BigDecimal.valueOf(1_000_000));
        return drops.setScale(0, BigDecimal.ROUND_HALF_UP).longValueExact();
    }

    private Object[] createMemoArray(String memo) {
        Map<String, Object> memoObj = new HashMap<>();
        Map<String, String> memoData = new HashMap<>();
        memoData.put("MemoData", hexEncode(memo));
        memoObj.put("Memo", memoData);
        return new Object[]{memoObj};
    }

    private String hexEncode(String input) {
        StringBuilder hex = new StringBuilder();
        for (byte b : input.getBytes()) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    @Override
    public AccountInfoResult getAccountInfo(XrplClient xrplClient, Address address) {
        try {
            final AccountInfoRequestParams params = AccountInfoRequestParams.builder()
                    .account(address)
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .build();

            return xrplClient.accountInfo(params);
        } catch (WebClientResponseException e) {
            log.error("Failed to get account info from XRPL RPC: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to get account info: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to get account info for address: {}", address, e);
            throw new RuntimeException("Failed to get account info", e);
        }
    }

    // PRIVATE METHODS ---

    private UnsignedLong instantToXrpTimestamp(Instant instant) {
        return UnsignedLong.valueOf(instant.getEpochSecond() - 0x386d4380);
    }

    private Instant xrpTimestampToInstant(UnsignedLong xrpTimeStamp) {
        return Instant.ofEpochSecond(xrpTimeStamp.plus(UnsignedLong.valueOf(0x386d4380)).longValue());
    }

    private Instant getMinExpirationTime(XrplClient xrplClient) {
        LedgerResult result = getValidatedLedger(xrplClient);
        Instant closeTime = xrpTimestampToInstant(
                result.ledger().closeTime()
                        .orElseThrow(() ->
                                new RuntimeException("Ledger close time must be present to calculate a minimum expiration time.")
                        )
        );

        Instant now = Instant.now();
        return closeTime.isBefore(now) ? now : closeTime;
    }

    private LedgerResult getValidatedLedger(XrplClient xrplClient) {
        try {
            LedgerRequestParams params = LedgerRequestParams.builder()
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .build();
            return xrplClient.ledger(params);
        } catch (JsonRpcClientErrorException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private long getSafeFinishAfter(long second) {
        // 현재 UTC 시각
        Instant now = Instant.now();

        // Ripple Epoch (2000-01-01T00:00:00Z)
        ZonedDateTime rippleEpoch = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        long rippleEpochSeconds = rippleEpoch.toEpochSecond();

        // 현재 시각을 Ripple Epoch 기준 초로 변환
        long nowSeconds = now.getEpochSecond();
        long finishAfter = nowSeconds - rippleEpochSeconds;

        // 안전하게 60초(1분) 추가
        long safeFinishAfter = finishAfter + second;

        System.out.println("최소 안전 FinishAfter 값: " + safeFinishAfter);

        return safeFinishAfter;
    }

}