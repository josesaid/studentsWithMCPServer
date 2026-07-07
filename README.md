# Students · MCP Server + REST API + Claude Agent

Microservicio **Spring Boot 3.5** que gestiona estudiantes y demuestra, de punta a punta, cómo un **MCP Server** ([Model Context Protocol](https://modelcontextprotocol.io)) expone la lógica de negocio como *tools* que un modelo de lenguaje —**Claude** (Anthropic)— puede invocar de forma autónoma vía *tool calling*.

El mismo proyecto contiene, sin duplicar lógica de negocio:

- Una **API REST** clásica (`/api/v1/students`) documentada con Swagger.
- Un **MCP Server** (transporte *Streamable HTTP* en `/mcp`) que publica 6 tools.
- Un **MCP Client** embebido + un **agente de Claude** que consume esas tools y resuelve instrucciones en lenguaje natural.

---

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [MCP Server: tools expuestas](#mcp-server-tools-expuestas)
- [API REST](#api-rest)
- [Cómo ejecutar](#cómo-ejecutar)
- [La demo end-to-end](#la-demo-end-to-end)
- [Configuración](#configuración)
- [Nota técnica: por qué el server exponía 0 tools](#nota-técnica-por-qué-el-server-exponía-0-tools)
- [Docker](#docker)
- [Testing](#testing)

---

## Arquitectura

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        studentsWithMCPServer (:8101)                       │
│                                                                            │
│   REST clients ──► StudentsController ─┐                                   │
│                                        ├─► StudentsService ─► Repository ─► H2
│   MCP tools ─────► StudentsMcpTools ───┘        (lógica de negocio única)  │
│                                                                            │
│   ┌───────────────────── MCP Server (/mcp, Streamable HTTP) ────────────┐ │
│   │  McpToolsConfig → List<SyncToolSpecification> → 6 tools registradas  │ │
│   └─────────────────────────────────────────────────────────────────────┘│
│                              ▲                                             │
│                              │ tools/list · tools/call (JSON-RPC)          │
│   ┌──────────────────────────┴──────────────────────────────────────────┐ │
│   │  MCP Client (AppClientSample) ──► StudentCreationAgent ──► Claude     │ │
│   │      handshake + listTools()          (Spring AI tool calling)        │ │
│   └───────────────────────────────────────────────────────────────────────┘
└──────────────────────────────────────────────────────────────────────────┘
```

**Principio clave:** tanto los endpoints REST como las tools MCP delegan en el mismo `StudentsService`. No hay lógica de negocio duplicada — MCP y REST son dos *fachadas* sobre el mismo núcleo.

---

## Stack tecnológico

| Componente | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje base |
| Spring Boot | 3.5.15 | Framework de aplicación |
| Spring AI | 1.1.8 | MCP Server/Client + integración con Anthropic |
| Spring Web (MVC) | — | API REST + transporte MCP Streamable HTTP |
| Spring Data JPA + Hibernate | — | Persistencia |
| H2 | runtime | Base de datos en memoria (dev) |
| MapStruct | 1.6.3 | Mapeo entity ↔ DTO |
| Lombok | 1.18.36 | Reducción de boilerplate |
| springdoc-openapi | 2.7.0 | Swagger UI / OpenAPI 3 |
| Anthropic Claude | `claude-sonnet-4-6` | Modelo del agente |

---

## Estructura del proyecto

```
src/main/java/com/mx/development/said/students/
├── StudentsApplication.java           # Punto de entrada Spring Boot
├── OpenApiConfig.java                 # Metadatos de Swagger/OpenAPI
│
├── controller/
│   ├── StudentsController.java        # API REST /api/v1/students
│   └── StudentsMcpDebugController.java # Inspección de tools vía REST (/api/v1/mcp/tools)
│
├── mcp/
│   ├── StudentsMcpTools.java          # 6 @Tool que delegan en StudentsService
│   └── McpToolsConfig.java            # Registra las tools en el MCP Server (ver nota técnica)
│
├── demo/
│   ├── AppClientSample.java           # CommandLineRunner: orquesta la demo end-to-end
│   ├── StudentCreationAgent.java      # Envía instrucción + tools a Claude (tool calling)
│   └── LoggingToolCallback.java       # Decorator que imprime cada invocación de tool
│
├── service/
│   ├── StudentsService.java           # Contrato de negocio
│   └── impl/StudentsServiceImpl.java  # Implementación
│
├── repository/StudentsRepository.java # Spring Data JPA
├── mapper/StudentsMapper.java         # MapStruct entity ↔ DTO
├── model/Students.java                # Entidad JPA
├── dto/
│   ├── StudentsRequestDTO.java        # Entrada (con Bean Validation)
│   └── StudentsResponseDTO.java       # Salida
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java    # 404 centralizado
```

---

## MCP Server: tools expuestas

Definidas en `StudentsMcpTools` con la anotación `@Tool` de Spring AI. Todas delegan en `StudentsService`.

| Tool | Descripción | Parámetros |
|---|---|---|
| `create_student` | Crea un nuevo estudiante | `firstName`, `lastName`, `email` (todos requeridos) |
| `get_student` | Obtiene un estudiante por su id | `id` (requerido) |
| `list_students` | Lista todos los estudiantes | — |
| `update_student` | Actualiza nombre/apellido/email | `id`, `firstName`, `lastName`, `email` |
| `delete_student` | Elimina un estudiante por id | `id` (requerido) |
| `count_students` | Devuelve el total de estudiantes | — |

- **Endpoint MCP (JSON-RPC / Streamable HTTP):** `http://localhost:8101/mcp`
- **Inspección rápida vía REST (sin hablar MCP):** `GET http://localhost:8101/api/v1/mcp/tools`

---

## API REST

Base: `http://localhost:8101/api/v1/students`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| `POST` | `/api/v1/students` | Crea un estudiante | `201 Created` + `StudentsResponseDTO` |
| `GET` | `/api/v1/students/{id}` | Obtiene por id | `200 OK` / `404` |
| `GET` | `/api/v1/students` | Lista todos | `200 OK` |
| `PUT` | `/api/v1/students/{id}` | Actualiza | `200 OK` / `404` |
| `DELETE` | `/api/v1/students/{id}` | Elimina | `204 No Content` |

**Modelo (`StudentsRequestDTO`):**

```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com"
}
```

Las entradas se validan con Bean Validation (`@NotNull` / `@NotBlank`). Los `ResourceNotFoundException` se traducen a `404` en `GlobalExceptionHandler`.

**Herramientas de exploración:**

- Swagger UI → http://localhost:8101/swagger-ui.html
- OpenAPI JSON → http://localhost:8101/v3/api-docs
- Consola H2 → http://localhost:8101/h2-console (JDBC: `jdbc:h2:mem:studentsdb`, user `sa`, sin password)

---

## Cómo ejecutar

### Requisitos

- JDK 21
- Maven 3.9+
- Una **API key de Anthropic** (solo si quieres el flujo completo con Claude)

### 1. Exportar la API key (opcional pero recomendado)

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

> Sin la key, el server MCP y la API REST funcionan igual; solo falla el paso final de conversación con Claude (Paso 3–6 de la demo), que queda capturado con un mensaje claro.

### 2. Arrancar

```bash
mvn spring-boot:run
```

La app queda en **http://localhost:8101**. Al arrancar, el `CommandLineRunner` ejecuta automáticamente la demo end-to-end en la consola.

---

## La demo end-to-end

`AppClientSample` (un `CommandLineRunner`) orquesta, en la consola, todo el circuito **MCP Client → MCP Server → Claude → tool real → DB**:

```
[Paso 1] Handshake "initialize" del MCP client contra el MCP Server (/mcp)
[Paso 2] tools/list → El MCP Server expone 6 tools:
           - create_student, get_student, list_students,
             update_student, delete_student, count_students
[Paso 3] Delega en StudentCreationAgent
[Paso 4] Las 6 tools quedan disponibles para Claude (tool calling)
[Paso 5] Prompt: "Crea un nuevo estudiante llamado Juan Pérez con correo juan.perez@example.com"
   >> Claude decide invocar create_student con {"firstName":"Juan","lastName":"Pérez",...}
      El MCP client envía "tools/call" → StudentsMcpTools → StudentsService → H2
[Paso 6] Respuesta final de Claude: "¡El estudiante ha sido creado exitosamente! ... ID: 1"
```

> El handshake `initialize` se dispara manualmente (no en el arranque del contexto) para garantizar que Tomcat ya esté escuchando en el 8101 antes de que el cliente se conecte —evitando una condición de carrera. Por eso `spring.ai.mcp.client.initialized=false`.

Cada invocación de tool se imprime paso a paso gracias a `LoggingToolCallback`, un *decorator* que envuelve cada `ToolCallback` sin alterar su comportamiento.

---

## Configuración

Todo en `src/main/resources/application.properties`. Puntos destacados:

```properties
server.port=8101

# --- MCP Server ---
spring.ai.mcp.server.name=students-mcp-server
spring.ai.mcp.server.protocol=STREAMABLE
spring.ai.mcp.server.type=SYNC

# --- MCP Client (demo, se conecta al propio server) ---
spring.ai.mcp.client.type=SYNC
spring.ai.mcp.client.initialized=false          # handshake manual (ver demo)
spring.ai.mcp.client.streamable-http.connections.students-server.url=http://localhost:8101
spring.ai.mcp.client.streamable-http.connections.students-server.endpoint=/mcp

# --- Anthropic (Claude) ---
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:}   # se toma del entorno; nunca se hardcodea
spring.ai.anthropic.chat.options.model=claude-sonnet-4-6
spring.ai.anthropic.chat.options.max-tokens=1024
```

Para **MySQL** en producción, hay un bloque comentado en el `.properties` listo para descomentar (y el `docker-compose.yml` ya prevé la red `scaffold-net`).

---

## Nota técnica: por qué el server exponía 0 tools

En Spring AI, **anotar métodos con `@Tool` dentro de un `@Component` no basta** para que el MCP Server los publique. El server solo registra tools que provengan de un bean `List<McpServerFeatures.SyncToolSpecification>` (consumido por `McpServerAutoConfiguration.mcpSyncServer(...)`).

Normalmente ese bean lo genera el auto-config `ToolCallbackConverterAutoConfiguration`, que agrega **todos** los `ToolCallback`/`ToolCallbackProvider` del contexto y los convierte a `SyncToolSpecification`. Pero ese mismo auto-config también invocaría, durante el arranque, el `SyncMcpToolCallbackProvider` del **cliente** (usado por el agente), disparando una conexión contra un Tomcat que aún no escucha → condición de carrera. Por eso está **excluido** en el `.properties`:

```properties
spring.autoconfigure.exclude=org.springframework.ai.mcp.server.common.autoconfigure.ToolCallbackConverterAutoConfiguration
```

El efecto colateral de excluirlo era que **nadie convertía las tools del server → 0 tools**.

**Solución** (`McpToolsConfig`): producir directamente el `List<SyncToolSpecification>` a partir de *solo* nuestras tools de negocio, sin tocar el provider del cliente:

```java
@Bean
public List<McpServerFeatures.SyncToolSpecification> studentsToolSpecifications(StudentsMcpTools tools) {
    ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
            .toolObjects(tools)
            .build()
            .getToolCallbacks();
    return McpToolUtils.toSyncToolSpecifications(callbacks);
}
```

Así se resuelven ambos problemas a la vez: el server publica sus 6 tools y se mantiene evitada la race condition del cliente.

---

## Docker

```bash
# Construir y levantar (usa la red externa scaffold-net)
./start_students.sh
# equivalente a: docker compose up --build -d
```

- **Dockerfile** multi-stage: build con `eclipse-temurin:21-jdk-alpine` + Maven, runtime con `eclipse-temurin:21-jre-alpine`.
- **docker-compose.yml** publica el puerto `8101` y espera a un Kafka global (`scaffold-kafka`) mediante un contenedor *health-check* antes de arrancar la app.
- La red `scaffold-net` es externa (creada por el entorno de scaffold); créala si no existe: `docker network create scaffold-net`.

---

## Testing

```bash
mvn test
```

Incluye `StudentsServiceTest` (capa de servicio). El build de Docker usa `-DskipTests` para acelerar la imagen; ejecuta los tests localmente con el comando anterior.

---

## Autor

**Jose Said** — proyecto demostrativo de integración **Spring AI + MCP + Claude**. Úsalo libremente como referencia.
