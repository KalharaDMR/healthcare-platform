package com.example.aisymptom_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpecializationClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<List<String>> getSpecializations() {
        return webClientBuilder.build()
                .get()
                .uri("http://admin-service/api/admin/specializations")
                .retrieve()
                .bodyToFlux(String.class)
                .collectList();
    }
}