package cl.pymetrack.msgateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/info")
    public Mono<Map<String, Object>> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "ms-gateway");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("timestamp", java.time.LocalDateTime.now());
        
        Map<String, Object> routes = new HashMap<>();
        routes.put("ms-admin", "http://localhost:8080");
        routes.put("ms-auth", "http://localhost:8082");
        routes.put("ms-pedidos", "http://localhost:8081");
        routes.put("ms-bff", "http://localhost:8084");
        info.put("routes", routes);
        
        return Mono.just(info);
    }

    @GetMapping("/routes")
    public Mono<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use /actuator/gateway/routes for detailed route information");
        response.put("actuator-endpoint", "/actuator/gateway/routes");
        
        return Mono.just(response);
    }
}
