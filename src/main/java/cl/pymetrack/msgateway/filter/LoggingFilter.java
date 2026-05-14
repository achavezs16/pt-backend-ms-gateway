package cl.pymetrack.msgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class LoggingFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();
        
        String requestId = java.util.UUID.randomUUID().toString();
        String clientIp = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        
        // Log de entrada
        logger.info("[{}] {} {} from {} - Request started", 
                requestId, request.getMethod(), request.getPath(), clientIp);
        
        // Agregar headers personalizados
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .header("X-Start-Time", String.valueOf(startTime.toEpochMilli()))
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doOnSuccess(aVoid -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.info("[{}] {} {} - Completed in {}ms", 
                            requestId, request.getMethod(), request.getPath(), duration.toMillis());
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.error("[{}] {} {} - Error after {}ms: {}", 
                            requestId, request.getMethod(), request.getPath(), duration.toMillis(), error.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return -200; // Ejecutar primero
    }
}
