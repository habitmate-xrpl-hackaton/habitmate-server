package com.example.xrpl.participation.api;

import com.example.xrpl.participation.application.MyParticipationCommandService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AiProofVerifier {

    private record VerificationResult(
            @JsonProperty("isSuccess") boolean isSuccess,
            @JsonProperty("reason") String reason
    ) {}

    private static final Logger logger = LoggerFactory.getLogger(AiProofVerifier.class);
    private final Client client;
    private final String modelName;
    private final ObjectMapper objectMapper;
    private final MyParticipationCommandService myParticipationCommandService;

    public AiProofVerifier(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model.name}") String modelName,
            ObjectMapper objectMapper,
            MyParticipationCommandService myParticipationCommandService
    ) {
        this.client = Client.builder().apiKey(apiKey).build();
        this.modelName = modelName;
        this.objectMapper = objectMapper;
        this.myParticipationCommandService = myParticipationCommandService;
    }

    @Async
    public void verifyProof(ProofAddedEvent event, String challengeContextAsJson) {
        try {
            String prompt = createInferencePrompt(challengeContextAsJson);
            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromUri(event.imageUrl(), "image/jpeg")
            );

            GenerateContentResponse response = client.models.generateContent(this.modelName, content, null);
            String responseJson = response.text();
            logger.info("Inferred JSON response for challengeId {}: {}", event.challengeId(), responseJson);

            VerificationResult result = objectMapper.readValue(responseJson, VerificationResult.class);

            myParticipationCommandService.verifyProof(event.participantId(), event.proofId(), result.isSuccess());

        } catch (Exception e) {
            logger.error("Error during AI inference for challengeId: {}", event.challengeId(), e);
            myParticipationCommandService.verifyProof(event.participantId(), event.proofId(), false);
        }
    }

    private String createInferencePrompt(String challengeContextAsJson) {
        return """
        You are an intelligent AI judge responsible for determining the success of a challenge.

        Comprehensively analyze the 'Challenge Information (JSON)' provided below to understand the implicit success rules and intent of this challenge.
        Then, determine if the provided image complies with the rules you have inferred, and you must respond *only* in the specified JSON format.

        ## Challenge Information (JSON)
        ```json
        %s
        ```

        ## Analysis and Judgment Guidelines
        1. **Rule Inference:** Identify the core objective of the challenge, focusing on the `title`, `description`, and `rules` fields.
        2. **Image Assessment:** Objectively evaluate whether the image is appropriate according to the inferred objective.
        3. **JSON Response:** Generate your response strictly according to the format below.

        ## Response Format (JSON)
        {
          "isSuccess": boolean
        }
        """.formatted(challengeContextAsJson);
    }
}
