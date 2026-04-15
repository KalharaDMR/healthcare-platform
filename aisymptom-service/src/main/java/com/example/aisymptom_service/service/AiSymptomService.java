package com.example.aisymptom_service.service;
import com.example.aisymptom_service.client.SpecializationClient;
import com.example.aisymptom_service.dto.AiResponseDto;
import com.example.aisymptom_service.dto.PatientRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import reactor.core.scheduler.Schedulers;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSymptomService {

    private final SpecializationClient client;
    private final Cache<String, List<String>> cache;
    private final GeminiService geminiService;
    public Mono<AiResponseDto> analyze(PatientRequest request) {

        return getSpecializations().map(specs->buildPrompt(request,specs))
                .flatMap(geminiService::generateContent)
                .map(this::parseResponse);
    }

    private Mono<List<String>> getSpecializations() {


        List<String> cached = cache.getIfPresent("SPECIALIZATIONS");

        if (cached != null) {
            return Mono.just(cached);
        }

        return client.getSpecializations()
                .doOnNext(list -> cache.put("SPECIALIZATIONS", list))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(List.of("General Physician"));
                });

    }

    private String buildPrompt(PatientRequest req, List<String> specs) {
        String name =  """
        You are a medical AI assistant.
    
        SYSTEM RULES:
        - Do NOT reveal your internal reasoning or thinking process.
        - Do NOT explain how you arrived at the answer.
        - Return ONLY valid JSON. Do not include any extra text, explanations, or formatting outside JSON.
        - Only provide patient-friendly explanations.
        - Use simple and clear English.
        - confidenceScore should be between 0.0 (low confidence) and 1.0 (high confidence)
        - If no suitable specialization is found, return an empty list and low confidenceScore
        - Recommend ONLY the top 3 most relevant specializations.
        - If symptoms are unclear, vague, or insufficient, return an empty list for recommendedSpecializations.
    
        PATIENT DATA:
        - Age: %d
        - Gender: %s
        - Symptoms: %s
    
        AVAILABLE SPECIALIZATIONS:
        %s
    
        TASK:
        Based on the patient data, recommend the most suitable medical specializations.
    
        For each recommended specialization, include:
        - A short, clear explanation for the patient
        - Why this specialization is suitable for their symptoms
    
        OUTPUT FORMAT (STRICT JSON ONLY):
    
        {
          "recommendedSpecializations": [
              {
                 "specialization": "",
                 "reason": ""
              }
           ],
          "generalAdvice": "",
          "confidenceScore": 0.0
        }
        """.formatted(
                req.getAge(),
                req.getGender(),
                String.join(", ", req.getSymptoms()),
                String.join(", ",specs)
        );
        System.out.println("-------------------Prompt--------------------"+name);
        return name;
    }


    private AiResponseDto parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Sometimes Gemini returns text with extra formatting → clean if needed
            String cleaned = response.trim()
                    .replace("```json", "")
                    .replace("```", "");

            return mapper.readValue(cleaned, AiResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

}

