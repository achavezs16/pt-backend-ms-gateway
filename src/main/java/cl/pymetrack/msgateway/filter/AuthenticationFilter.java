package cl.pymetrack.msgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    // Rutas que no requieren autenticación
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/health",
            "/api/v1/auth/change-password", // Primer login no requiere token
            "/api-docs",
            "/swagger-ui",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        logger.debug("Procesando solicitud: {} {}", request.getMethod(), path);

        // Verificar si la ruta es pública
        if (isPublicPath(path)) {
            logger.debug("Ruta pública permitida: {}", path);
            return chain.filter(exchange);
        }

        // Verificar token para rutas protegidas
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Token no proporcionado para ruta protegida: {}", path);
            return handleUnauthorized(exchange);
        }

        String token = authHeader.substring(7);
        
        // Validación básica del token
        if (!isValidToken(token)) {
            logger.warn("Token inválido para ruta: {}", path);
            return handleUnauthorized(exchange);
        }

        // Agregar información del usuario al request para downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", extractEmailFromToken(token))
                .header("X-User-Id", extractUserIdFromToken(token))
                .header("X-User-Role", "PYME")
                .build();

        logger.debug("Token válido, continuando con: {}", path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidToken(String token) {
        // Validación básica - en producción, aquí validaríamos el JWT
        return token != null && !token.trim().isEmpty() && token.length() > 10;
    }

    private String extractEmailFromToken(String token) {
        // En una implementación real, decodificaríamos el JWT
        // Por ahora, retornamos un placeholder
        return "user@example.com";
    }

    private String extractUserIdFromToken(String token) {
        // En una implementación real, extraeríamos el ID del JWT
        // Por ahora, retornamos un hash del token
        return String.valueOf(Math.abs(token.hashCode()));
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Token inválido o no proporcionado\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // Ejecutar antes que otros filtros
    }
}
