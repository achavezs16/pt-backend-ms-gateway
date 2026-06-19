package cl.pymetrack.msgateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
    }

    @Test
    void testFilter_FlujoExitoso_AgregaHeadersYRegistraTiempo() {
        // Arrange: Simulamos una petición con una IP remota específica
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pymes/pedidos")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Simulamos que el siguiente filtro responde con éxito (Mono.empty)
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act & Assert: Verificamos la ejecución reactiva
        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Capturamos el exchange modificado para verificar que se agregaron los headers
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerWebExchange mutatedExchange = captor.getValue();
        assertNotNull(mutatedExchange.getRequest().getHeaders().getFirst("X-Request-Id"), "Falta el X-Request-Id");
        assertNotNull(mutatedExchange.getRequest().getHeaders().getFirst("X-Start-Time"), "Falta el X-Start-Time");
    }

    @Test
    void testFilter_FlujoConError_EjecutaDoOnError() {
        // Arrange: Petición sin remoteAddress para cubrir la rama "unknown" de tu ternario
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pymes/error").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Simulamos que el microservicio falla y retorna un error
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.error(new RuntimeException("Error simulado de enrutamiento")));

        // Act & Assert: Verificamos que el error se propaga correctamente
        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .expectErrorMessage("Error simulado de enrutamiento")
                .verify();

        // Verificamos que sí se intentó llamar a la cadena
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testGetOrder_DebeRetornarMenos200() {
        // Arrange & Act & Assert
        assertEquals(-200, loggingFilter.getOrder(), "El orden de ejecución debe ser -200");
    }
}