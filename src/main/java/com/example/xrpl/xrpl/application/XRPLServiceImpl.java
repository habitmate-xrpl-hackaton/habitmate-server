package com.example.xrpl.xrpl.application;

import com.example.xrpl.xrpl.config.XRPLConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
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
    public void completeBatchEscrow(List<EscrowParams> escrows) {
        try {
            final XrplClient xrplClient = xrplConfig.getXrplClient();
            final KeyPair mainWalletKeyPair = xrplConfig.getCentralWalletKeyPair();
            final BcSignatureService signatureService = new BcSignatureService();
            final FeeResult feeResult = xrplClient.fee();

            final AccountInfoResult mainAccountInfo = getAccountInfo(xrplClient, mainWalletKeyPair.publicKey().deriveAddress());

            for (int i = 0; i < escrows.size(); i++) {
                EscrowParams escrowParam = escrows.get(i);
                UnsignedInteger currentSequence = mainAccountInfo.accountData().sequence();

                log.info("Starting batch escrow completion of {} escrows from sequence {}", escrows.size(), currentSequence);
                
                try {
                    final EscrowFinish escrowFinish = EscrowFinish.builder()
                            .account(mainWalletKeyPair.publicKey().deriveAddress())
                            .fee(feeResult.drops().openLedgerFee())
                            .sequence(currentSequence)
                            .owner(Address.of(escrowParam.escrowOwner()))
                            .offerSequence(UnsignedInteger.valueOf(escrowParam.offerSequence()))
                            .signingPublicKey(mainWalletKeyPair.publicKey())
                            .build();

                    final SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
                            mainWalletKeyPair.privateKey(), escrowFinish);

                    final SubmitResult<EscrowFinish> result = xrplClient.submit(signedEscrowFinish);
                    
                    if ("tesSUCCESS".equals(result.engineResult())) {
                        log.info("Escrow completion {}/{} successful: Owner: {}, OfferSequence: {} - Hash: {}", 
                                i + 1, escrows.size(),
                                escrowParam.escrowOwner(),
                                escrowParam.offerSequence(),
                                result.transactionResult().hash());
                    } else {
                        log.error("Escrow completion {}/{} failed: {} - {}", 
                                i + 1, escrows.size(), result.engineResult(), result.engineResultMessage());
                        throw new RuntimeException("Escrow completion failed: " + result.engineResult());
                    }

                    currentSequence = currentSequence.plus(UnsignedInteger.ONE);

                    if (i < escrows.size() - 1) {
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    log.error("Failed to complete escrow {}/{}: Owner: {}, OfferSequence: {}", 
                            i + 1, escrows.size(), 
                            escrowParam.escrowOwner(),
                            escrowParam.offerSequence(), e);
                    throw new RuntimeException("Failed to complete escrow " + (i + 1) + ": " + e.getMessage(), e);
                }
            }

            log.info("Successfully completed batch escrow completion of {} escrows", escrows.size());

        } catch (Exception e) {
            log.error("Failed to complete batch escrow", e);
            throw new RuntimeException("Failed to complete batch escrow", e);
        }
    }

    @Override
    public void sendBatchPayment(List<PaymentParams> payments) {
        try {
            final XrplClient xrplClient = xrplConfig.getXrplClient();
            final KeyPair mainWalletKeyPair = xrplConfig.getCentralWalletKeyPair();
            final BcSignatureService signatureService = new BcSignatureService();
            final FeeResult feeResult = xrplClient.fee();
            
            // 계정 정보 및 시작 sequence 확인
            final AccountInfoResult mainAccountInfo = getAccountInfo(xrplClient, mainWalletKeyPair.publicKey().deriveAddress());
            UnsignedInteger currentSequence = mainAccountInfo.accountData().sequence();
            
            log.info("Starting batch payment of {} transactions from sequence {}", payments.size(), currentSequence);
            
            // 각 payment를 개별 Payment 트랜잭션으로 순차 전송
            for (int i = 0; i < payments.size(); i++) {
                PaymentParams paymentParam = payments.get(i);
                
                try {
                    // Payment 트랜잭션 빌드 - 기존 sendPayment와 완전히 동일한 방식
                    Payment payment;
                    if (paymentParam.destinationTag() != null && paymentParam.memo() != null) {
                        // 둘 다 있는 경우
                        payment = Payment.builder()
                                .account(mainWalletKeyPair.publicKey().deriveAddress())
                                .fee(feeResult.drops().openLedgerFee())
                                .sequence(currentSequence)
                                .destination(Address.of(paymentParam.destinationAddress()))
                                .destinationTag(UnsignedInteger.valueOf(paymentParam.destinationTag()))
                                .amount(XrpCurrencyAmount.ofDrops(xrpToDrops(paymentParam.amount())))
                                .signingPublicKey(mainWalletKeyPair.publicKey())
                                .addMemos(MemoWrapper.builder()
                                        .memo(Memo.builder()
                                                .memoData(paymentParam.memo())
                                                .build())
                                        .build())
                                .build();
                    } else if (paymentParam.destinationTag() != null) {
                        // destinationTag만 있는 경우
                        payment = Payment.builder()
                                .account(mainWalletKeyPair.publicKey().deriveAddress())
                                .fee(feeResult.drops().openLedgerFee())
                                .sequence(currentSequence)
                                .destination(Address.of(paymentParam.destinationAddress()))
                                .destinationTag(UnsignedInteger.valueOf(paymentParam.destinationTag()))
                                .amount(XrpCurrencyAmount.ofDrops(xrpToDrops(paymentParam.amount())))
                                .signingPublicKey(mainWalletKeyPair.publicKey())
                                .build();
                    } else if (paymentParam.memo() != null && !paymentParam.memo().trim().isEmpty()) {
                        // memo만 있는 경우
                        payment = Payment.builder()
                                .account(mainWalletKeyPair.publicKey().deriveAddress())
                                .fee(feeResult.drops().openLedgerFee())
                                .sequence(currentSequence)
                                .destination(Address.of(paymentParam.destinationAddress()))
                                .amount(XrpCurrencyAmount.ofDrops(xrpToDrops(paymentParam.amount())))
                                .signingPublicKey(mainWalletKeyPair.publicKey())
                                .addMemos(MemoWrapper.builder()
                                        .memo(Memo.builder()
                                                .memoData(paymentParam.memo())
                                                .build())
                                        .build())
                                .build();
                    } else {
                        // 둘 다 없는 경우
                        payment = Payment.builder()
                                .account(mainWalletKeyPair.publicKey().deriveAddress())
                                .fee(feeResult.drops().openLedgerFee())
                                .sequence(currentSequence)
                                .destination(Address.of(paymentParam.destinationAddress()))
                                .amount(XrpCurrencyAmount.ofDrops(xrpToDrops(paymentParam.amount())))
                                .signingPublicKey(mainWalletKeyPair.publicKey())
                                .build();
                    }
                    
                    // 트랜잭션 서명
                    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
                            mainWalletKeyPair.privateKey(), payment);
                    
                    // 트랜잭션 제출
                    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
                    
                    if ("tesSUCCESS".equals(result.engineResult())) {
                        log.info("Payment {}/{} successful: {} -> {} ({} XRP) - Hash: {}", 
                                i + 1, payments.size(),
                                mainWalletKeyPair.publicKey().deriveAddress(),
                                paymentParam.destinationAddress(), 
                                paymentParam.amount(),
                                result.transactionResult().hash());
                    } else {
                        log.error("Payment {}/{} failed: {} - {}", 
                                i + 1, payments.size(), result.engineResult(), result.engineResultMessage());
                        throw new RuntimeException("Payment failed: " + result.engineResult());
                    }
                    
                    // 다음 sequence 번호로 증가
                    currentSequence = currentSequence.plus(UnsignedInteger.ONE);
                    
                    // 네트워크 부하 방지를 위한 짧은 대기 (선택적)
                    if (i < payments.size() - 1) {
                        Thread.sleep(100); // 100ms 대기
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to send payment {}/{}: {} -> {}", 
                            i + 1, payments.size(), 
                            mainWalletKeyPair.publicKey().deriveAddress(),
                            paymentParam.destinationAddress(), e);
                    throw new RuntimeException("Failed to send payment " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
            
            log.info("Successfully completed batch payment of {} transactions", payments.size());
            
        } catch (Exception e) {
            log.error("Failed to send batch payment", e);
            throw new RuntimeException("Failed to send batch payment", e);
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
}