# ms-gateway - API Gateway

API Gateway construido con Spring Cloud Gateway para el sistema PymeTrack. Actúa como punto único de entrada y enrutador centralizado para todos los microservicios.

## 🚀 Características

- ✅ **Enrutamiento centralizado** para todos los microservicios
- ✅ **Spring Cloud Gateway** con configuración programática
- ✅ **Filtros personalizados** para logging y autenticación
- ✅ **CORS configurado** para frontend
- ✅ **Fallback controllers** para manejo de errores
- ✅ **Actuator endpoints** para monitoreo
- ✅ **Documentación** con SpringDoc OpenAPI

## 📋 Requisitos

- Java 17+
- Maven 3.8+
- **ms-admin** corriendo en localhost:8080
- **ms-auth** corriendo en localhost:8082
- **ms-pedidos** corriendo en localhost:8081
- **ms-bff** (opcional) corriendo en localhost:8084
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0

## 🛠️ Configuración

### Microservicios Conectados
```yaml
# ms-admin (gestión de PYMEs)
ms-admin: http://localhost:8080

# ms-auth (autenticación JWT)
ms-auth: http://localhost:8082

# ms-pedidos (pedidos y productos)
ms-pedidos: http://localhost:8081

# ms-bff (Backend for Frontend)
ms-bff: http://localhost:8084
```

### Puerto del Servidor
```yaml
server:
  port: 8083
```

## 📚 Rutas Configuradas

### Gestión de PYMEs (ms-admin)
```
/api/v1/pymes/**      → http://localhost:8080
/api/v1/admin/**      → http://localhost:8080
```

### Autenticación (ms-auth)
```
/api/v1/auth/**       → http://localhost:8082
```

### Pedidos y Productos (ms-pedidos)
```
/api/v1/pedidos/**   → http://localhost:8081
/api/v1/productos/** → http://localhost:8081
```

### Backend for Frontend (ms-bff)
```
/api/v1/bff/**       → http://localhost:8084
```

## 🔄 Flujo de Peticiones

### Diagrama de Arquitectura
```
Frontend (3000)
    ↓
API Gateway (8083)
    ├──→ /api/v1/auth/** → ms-auth (8082)
    ├──→ /api/v1/pymes/** → ms-admin (8080)
    ├──→ /api/v1/pedidos/** → ms-pedidos (8081)
    └──→ /api/v1/bff/** → ms-bff (8084)
```

### Ejemplo de Petición
```bash
# Login a través del Gateway
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"contacto@techstore.cl","password":"Temp123!"}'

# Gateway enruta a: http://localhost:8082/api/v1/auth/login
```

## 🛡️ Seguridad

### Filtros Implementados

#### 1. AuthenticationFilter
- **Orden**: -100 (primero)
- **Función**: Valida tokens JWT en rutas protegidas
- **Rutas públicas**: `/api/v1/auth/login`, `/api/v1/auth/health`, `/api-docs`, `/swagger-ui`, `/actuator/health`

#### 2. LoggingFilter
- **Orden**: -200 (antes que todo)
- **Función**: Logging estructurado de todas las peticiones
- **Headers agregados**: `X-Request-Id`, `X-Start-Time`

### CORS Configuration
```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOriginPatterns: "*"
      allowedMethods: "*"
      allowedHeaders: "*"
      exposedHeaders: "Authorization"
      allowCredentials: true
      maxAge: 3600
```

## 📊 Monitoreo

### Actuator Endpoints
```bash
# Health check
curl http://localhost:8083/actuator/health

# Gateway routes
curl http://localhost:8083/actuator/gateway/routes

# Información del Gateway
curl http://localhost:8083/api/gateway/info

# Métricas
curl http://localhost:8083/actuator/metrics
```

### Endpoints Propios del Gateway
```bash
# Información del Gateway
GET /api/gateway/info

# Rutas disponibles
GET /api/gateway/routes

# Health check propio
GET /api/fallback/health
```

## 🚨 Manejo de Errores

### Fallback Controllers

Cuando un microservicio no está disponible, el Gateway retorna respuestas de fallback:

```json
{
  "service": "ms-admin",
  "status": "unavailable",
  "message": "El servicio de administración de PYMEs no está disponible temporalmente",
  "timestamp": "2024-05-02T13:45:00",
  "retryAfter": "30s"
}
```

### Endpoints de Fallback
```
GET /api/fallback/ms-admin    → Fallback para ms-admin
GET /api/fallback/ms-auth     → Fallback para ms-auth
GET /api/fallback/ms-pedidos  → Fallback para ms-pedidos
GET /api/fallback/ms-bff      → Fallback para ms-bff
```

## 🚀 Ejecución

### Desarrollo
```bash
mvn spring-boot:run
```

### Producción
```bash
mvn clean package
java -jar target/ms-gateway-0.0.1-SNAPSHOT.jar
```

## 📖 Swagger UI

Accede a la documentación interactiva en:
```
http://localhost:8083/swagger-ui.html
```

## 🗂️ Estructura del Proyecto

```
src/main/java/cl/pymetrack/msgateway/
├── config/                 # Configuración del Gateway
│   ├── GatewayConfig.java
│   └── RouteConfig.java
├── filter/                 # Filtros personalizados
│   ├── AuthenticationFilter.java
│   └── LoggingFilter.java
├── controller/             # Endpoints del Gateway
│   ├── GatewayController.java
│   └── FallbackController.java
└── MsGatewayApplication.java
```

## 🔧 Configuración Avanzada

### Headers Agregados Automáticamente
```yaml
# Headers agregados a todas las peticiones
X-Gateway-Request-Id: UUID único
X-Gateway-Request-Time: Timestamp
X-Gateway-Service: Nombre del servicio destino
gateway=true  # Query parameter
```

### Logging
```yaml
logging:
  level:
    cl.pymetrack.msgateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
    reactor.netty: DEBUG
```

## 🐛 Troubleshooting

### Error: Servicio no disponible
```json
{
  "service": "ms-admin",
  "status": "unavailable",
  "message": "El servicio de administración de PYMEs no está disponible temporalmente"
}
```

**Solución:** Verificar que el microservicio correspondiente esté corriendo en el puerto correcto.

### Error: Token no proporcionado
```json
{
  "error": "Unauthorized",
  "message": "Token inválido o no proporcionado"
}
```

**Solución:** Incluir header `Authorization: Bearer <token>` en peticiones protegidas.

### Error: CORS
```json
{
  "error": "CORS policy error"
}
```

**Solución:** Verificar configuración de CORS en el Gateway o en el microservicio destino.

## 🤝 Integración con Frontend

### Configuración de Axios/HttpClient
```javascript
// Base URL del Gateway
const apiClient = axios.create({
  baseURL: 'http://localhost:8083/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

## 📈 Próximos Mejoras

1. **Circuit Breaker** - Implementar patlón Circuit Breaker
2. **Rate Limiting** - Limitar peticiones por cliente
3. **Load Balancing** - Balanceo de carga entre instancias
4. **JWT Validation** - Validación centralizada de tokens
5. **Health Checks** - Verificación de salud de servicios

## 📝 Notas Importantes

- **Estado sin estado**: El Gateway no mantiene estado entre peticiones
- **Reactividad**: Construido con WebFlux y programación reactiva
- **Escalabilidad**: Diseñado para escalar horizontalmente
- **Monitoreo**: Integración completa con Spring Actuator

## 🔗 Comunicación con Microservicios

### Headers de Contexto
El Gateway agrega headers que los microservicios pueden usar:
- `X-User-Email`: Email del usuario autenticado
- `X-User-Id`: ID del usuario autenticado
- `X-User-Role`: Rol del usuario
- `X-Request-Id`: ID único de la petición
- `X-Gateway-Service`: Servicio destino

### Verificación de Servicios
```bash
# Verificar que todos los servicios estén disponibles
for service in 8080 8081 8082 8084; do
  echo "Verificando puerto $service..."
  curl -s http://localhost:$service/actuator/health || echo "Servicio en puerto $service no disponible"
done
```

## 🧪 Pruebas y Cobertura (Quality Gate)

El proyecto cuenta con una rigurosa suite de pruebas unitarias enfocada en garantizar la seguridad y fiabilidad del enrutamiento reactivo, superando el **Quality Gate del 60%** exigido por los estándares de calidad.

### Herramientas Utilizadas
- **JUnit 5 & Mockito:** Para la creación de mocks y aserciones.
- **Reactor Test (`StepVerifier`):** Para la evaluación estricta de flujos asíncronos y reactivos (`Mono`, `Flux`).
- **MockServerWebExchange:** Para la simulación de peticiones HTTP nativas de WebFlux.
- **JaCoCo:** Para la medición y validación de cobertura de código.

### Cobertura Alcanzada
- **Cobertura Total del Proyecto:** **97%** 🏆
- **Cobertura en Filtros (`cl.pymetrack.msgateway.filter`):** **100%**
  - `AuthenticationFilter`: Validación estricta de tokens JWT, manejo de rutas públicas e inyección de headers de contexto (`X-User-Email`, `X-User-Role`).
  - `LoggingFilter`: Registro de tiempos de respuesta, trazabilidad (`X-Request-Id`) y manejo de errores reactivos.

### Ejecución de Pruebas
Para ejecutar las pruebas y generar el reporte HTML de JaCoCo:
```bash
mvn clean test jacoco:report
```

- **El reporte interactivo se generará en la ruta:** target/site/jacoco/index.html