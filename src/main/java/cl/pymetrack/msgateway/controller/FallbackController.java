package cl.pymetrack.msgateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/ms-admin")
    public Mono<Map<String, Object>> msAdminFallback(ServerWebExchange exchange) {
        logger.warn("Fallback activated for ms-admin service");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ms-admin");
        response.put("status", "unavailable");
        response.put("message", "El servicio de administración de PYMEs no está disponible temporalmente");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "30s");
        
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(response);
    }

    @GetMapping("/ms-auth")
    public Mono<Map<String, Object>> msAuthFallback(ServerWebExchange exchange) {
        logger.warn("Fallback activated for ms-auth service");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ms-auth");
        response.put("status", "unavailable");
        response.put("message", "El servicio de autenticación no está disponible temporalmente");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "30s");
        
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(response);
    }

    @GetMapping("/ms-pedidos")
    public Mono<Map<String, Object>> msPedidosFallback(ServerWebExchange exchange) {
        logger.warn("Fallback activated for ms-pedidos service");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ms-pedidos");
        response.put("status", "unavailable");
        response.put("message", "El servicio de pedidos no está disponible temporalmente");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "30s");
        
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(response);
    }

    @GetMapping("/ms-bff")
    public Mono<Map<String, Object>> msBffFallback(ServerWebExchange exchange) {
        logger.warn("Fallback activated for ms-bff service");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ms-bff");
        response.put("status", "unavailable");
        response.put("message", "El servicio BFF no está disponible temporalmente");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "30s");
        
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(response);
    }

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ms-gateway");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return Mono.just(health);
    }
}
