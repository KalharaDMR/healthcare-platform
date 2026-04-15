package com.example.aisymptom_service.service;

import com.example.aisymptom_service.dto.GeminiRequest;
import com.example.aisymptom_service.dto.GeminiResponse;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final WebClient externalWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String MODEL = "gemini-3-flash-preview";

    // ✅ MAIN METHOD (NON-BLOCKING)
    public Mono<String> generateContent(String prompt) {

        GeminiRequest request = buildRequest(prompt);

        return externalWebClient.post()
                .uri(uriBuilder -> uriBuilder.scheme("https").host("generativelanguage.googleapis.com")
                        .path("/v1beta/models/" + MODEL + ":generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(org.springframework.http.MediaType.valueOf(MediaType.APPLICATION_JSON))
                .bodyValue(request)

                // ✅ Handle API errors (4xx / 5xx)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new RuntimeException("Gemini API Error: " + errorBody))
                                )
                )

                // ✅ Convert response to Java object
                .bodyToMono(GeminiResponse.class)

                // ✅ Extract safely
                .map(this::extractTextSafe)

                // ✅ Timeout
                .timeout(Duration.ofSeconds(30))

                // ✅ Retry on failure
                .retry(2)

                // ✅ Final fallback
                .onErrorResume(ex -> {
                    System.out.println("Gemini Error: " + ex.getMessage());
                    return Mono.just("""
                    {
                      "recommendedSpecializations": [],
                      "generalAdvice": "Unable to process request right now.",
                      "confidenceScore": 0.0
                    }
                    """);
                });
    }

    // ✅ Build Gemini request
    private GeminiRequest buildRequest(String prompt) {

        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(prompt);

        GeminiRequest.Content content = new GeminiRequest.Content();
        content.setParts(List.of(part));

        GeminiRequest request = new GeminiRequest();
        request.setContents(List.of(content));

        return request;
    }

    // ✅ Safe extraction (NO CRASH)
    private String extractTextSafe(GeminiResponse response) {

        if (response == null ||
                response.getCandidates() == null ||
                response.getCandidates().isEmpty()) {
            return "⚠️ No response from AI.";
        }

        var candidate = response.getCandidates().get(0);

        if (candidate.getContent() == null ||
                candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {
            return "⚠️ Invalid AI response structure.";
        }

        return candidate.getContent()
                .getParts()
                .get(0)
                .getText();
    }
}
