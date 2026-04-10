package com.healthcare.gateway.filter;

import com.healthcare.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip auth endpoints (no token required)
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/doctor/search")
                || path.startsWith("/api/doctor/availability/public/")) {
            return chain.filter(exchange);
        }
        // Extract token from Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            System.out.println("----------------------------UNAUTHORIZEDDDD---------------------------------------------------------------------------------------------");
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            System.out.println("-------------------------------------------Rusiraaaaaaaaa--------------------------------------------------------------------------------");
            return exchange.getResponse().setComplete();
        }

        // Extract username and maybe roles from token (we'll just forward username)
        String username = jwtUtil.extractUsername(token);
        // Here you could also extract roles from token claims if needed

        // Add headers to downstream request
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header("X-User-Id", username)
                        .header("X-User-Role", extractRoles(token))) // extract roles if present
                .build();

        return chain.filter(mutatedExchange);
    }

    private String extractRoles(String token) {
        Claims claims = jwtUtil.extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            return rolesList.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    @Override
    public int getOrder() {
        return -1; // Execute early
    }
}