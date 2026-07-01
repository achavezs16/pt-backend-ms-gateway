package cl.pymetrack.msgateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        // Inicializamos el filtro antes de cada prueba
        authenticationFilter = new AuthenticationFilter();
    }

    @Test
    void testFilter_RutaPublica_DebePasarSinToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Verificamos que se llamó a la cadena de filtros permitiendo el paso
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void testFilter_RutaProtegida_SinToken_DebeRetornarUnauthorized() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pymes/dashboard").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Verificamos que se cortó la ejecución y retornó 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testFilter_RutaProtegida_TokenInvalido_DebeRetornarUnauthorized() {
        // Arrange (token de menos de 10 caracteres)
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pymes/dashboard")
                .header("Authorization", "Bearer corto")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Verificamos que detectó el token inválido y retornó 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testFilter_RutaProtegida_TokenValido_DebeMutarRequestYPasar() {
        // Arrange (token mayor a 10 caracteres)
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pymes/dashboard")
                .header("Authorization", "Bearer un_token_super_valido_y_largo")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Capturamos el exchange modificado para verificar que se agregaron los headers
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerWebExchange mutatedExchange = captor.getValue();
        assertEquals("user@example.com", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Email"));
        assertEquals("PYME", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role"));
        assertNotNull(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
    }

    @Test
    void testGetOrder_DebeRetornarMenos100() {
        assertEquals(-100, authenticationFilter.getOrder());
    }
}