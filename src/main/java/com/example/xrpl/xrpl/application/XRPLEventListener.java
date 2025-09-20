package com.example.xrpl.xrpl.application;

import com.example.xrpl.catalog.api.CatalogQueryService;
import com.example.xrpl.catalog.api.ChallengeCompletedEvent;
import com.example.xrpl.catalog.api.ChallengeDetailDto;
import com.example.xrpl.participation.api.ParticipationQueryService;
import com.example.xrpl.user.api.UserQueryService;
import com.example.xrpl.xrpl.api.XRPLService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class XRPLEventListener {

    private final XRPLService xrplService;
    private final ParticipationQueryService participationQueryService;
    private final UserQueryService userQueryService;
    private final CatalogQueryService catalogQueryService;

    @ApplicationModuleListener
    public void handleChallengeCompleted(ChallengeCompletedEvent event) {
        long challengeId = event.challengeId();

        List<ParticipationQueryService.ParticipantInfo> participants = participationQueryService.findParticipantsByChallengeId(challengeId);
        if (participants.isEmpty()) {
            return; // No participants, nothing to do.
        }

        // 1. Complete all escrows for the challenge.
        completeAllEscrows(participants);

        // 2. Process refunds for eligible participants.
        processRefunds(challengeId, participants);
    }

    /**
     * Gathers all escrows from participants and triggers a batch completion.
     */
    private void completeAllEscrows(List<ParticipationQueryService.ParticipantInfo> participants) {
        List<XRPLService.EscrowParams> escrowsToComplete = participants.stream()
                .map(p -> new XRPLService.EscrowParams(p.escrowOwner(), Integer.parseInt(p.offerSequence())))
                .collect(Collectors.toList());

        xrplService.completeBatchEscrow(escrowsToComplete);
    }

    /**
     * Identifies participants eligible for a refund and triggers a batch payment.
     */
    private void processRefunds(long challengeId, List<ParticipationQueryService.ParticipantInfo> participants) {
        ChallengeDetailDto challengeDetails = catalogQueryService.findChallengeById(challengeId);
        int totalProofCount = challengeDetails.totalProofCount();

        // Filter for participants who met the 70% proof threshold.
        List<Long> eligibleUserIds = participants.stream()
                .filter(p -> (p.passedProofCount() * 1.0 / totalProofCount) >= 0.7)
                .map(ParticipationQueryService.ParticipantInfo::userId)
                .collect(Collectors.toList());

        if (eligibleUserIds.isEmpty()) {
            return; // No one is eligible for a refund.
        }

        // Fetch wallet addresses for the eligible users.
        Map<Long, String> userWallets = userQueryService.findWalletAddressesByUserIds(eligibleUserIds);

        // Calculate the refund amount (70% of the entry fee).
        BigDecimal refundAmount = challengeDetails.entryFee().amount().multiply(new BigDecimal("0.7"));

        // Construct payment parameters and send the batch payment.
        List<XRPLService.PaymentParams> payments = eligibleUserIds.stream()
                .map(userId -> {
                    String walletAddress = userWallets.get(userId);
                    return new XRPLService.PaymentParams(walletAddress, null, refundAmount, "Challenge Refund");
                })
                .collect(Collectors.toList());

        if (!payments.isEmpty()) {
            xrplService.sendBatchPayment(payments);
        }
    }
}
