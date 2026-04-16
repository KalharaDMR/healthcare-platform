package com.example.aisymptom_service.controller;
import com.example.aisymptom_service.dto.AiResponseDto;
import com.example.aisymptom_service.dto.PatientRequest;
import com.example.aisymptom_service.service.AiSymptomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiSymptomService service;

    @PostMapping("/analyze")
    public Mono<ResponseEntity<AiResponseDto>> analyze(
            @Valid @RequestBody PatientRequest request) {

        return service.analyze(request)
                .map(ResponseEntity::ok);
    }
}