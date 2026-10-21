# studentsWithMCPServer

**Project page:** https://josesaid.github.io/studentsWithMCPServer/

A **Spring Boot 3.5** microservice that manages students and shows, end to end, how an **MCP server** ([Model Context Protocol](https://modelcontextprotocol.io)) can expose business logic as *tools* that a language model (**Claude** from Anthropic) calls autonomously through *tool calling*.

The same project provides, without duplicating business logic:

- A classic **REST API** (`/api/v1/students`) documented with Swagger UI.
- An **MCP server** (Streamable HTTP transport on `/mcp`) that publishes 6 tools.
- An embedded **MCP client** and a **Claude agent** that consumes those tools and resolves natural-language instructions.

---

## Table of contents

- [Architecture](#architecture)
- [Stack](#stack)
- [Project structure](#project-structure)
- [MCP server tools](#mcp-server-tools)
- [REST API](#rest-api)
- [Running the application](#running-the-application)
- [End-to-end demo](#end-to-end-demo)
- [Configuration](#configuration)
- [Technical note: why the server first exposed 0 tools](#technical-note-why-the-server-first-exposed-0-tools)
- [Docker](#docker)
- [Testing](#testing)
- [Known limitations](#known-limitations)

---

## Architecture

```
REST clients ──► StudentsController ─┐
                                     ├─► StudentsService ─► StudentsRepository ─► H2 (in memory)
MCP tools ─────► StudentsMcpTools ───┘        (single business-logic layer)

MCP server (/mcp, Streamable HTTP)
  McpToolsConfig → List<SyncToolSpecification> → 6 registered tools

MCP client (AppClientSample) ──► StudentCreationAgent ──► Claude
  handshake + listTools()        (Spring AI tool calling)
```

**Key principle:** REST endpoints and MCP tools both delegate to the same `StudentsService`. MCP and REST are two facades over the same core.

---

## Stack

| Component | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.5.15 | Application framework |
| Spring AI | 1.1.8 | MCP server and client, Anthropic integration |
| Spring Web (MVC) | — | REST API and MCP Streamable HTTP transport |
| Spring Data JPA and Hibernate | — | Persistence |
| H2 | runtime | In-memory database (development) |
| MapStruct | 1.6.3 | Entity to DTO mapping |
| Lombok | 1.18.36 | Boilerplate reduction |
| springdoc-openapi | 2.7.0 | Swagger UI and OpenAPI 3 |
| Anthropic Claude | `claude-sonnet-4-6` | Agent model |

**Messaging:** Not used in the application code. The Docker Compose file contains a Kafka health-check container and Kafka environment variables, but the application has no Kafka dependency.

**Cloud:** Not used. The agent calls the Anthropic API.

---

## Project structure

```
src/main/java/com/mx/development/jose/said/
├── StudentsApplication.java             # Spring Boot entry point
├── OpenApiConfig.java                   # Swagger / OpenAPI metadata
├── controller/
│   ├── StudentsController.java          # REST API /api/v1/students
│   └── StudentsMcpDebugController.java  # Tool inspection over REST (/api/v1/mcp/tools)
├── mcp/
│   ├── StudentsMcpTools.java            # 6 @Tool methods that delegate to StudentsService
│   └── McpToolsConfig.java              # Registers the tools on the MCP server (see technical note)
├── demo/
│   ├── AppClientSample.java             # CommandLineRunner that runs the end-to-end demo
│   ├── StudentCreationAgent.java        # Sends the instruction and tools to Claude (tool calling)
│   └── LoggingToolCallback.java         # Decorator that logs every tool invocation
├── service/
│   ├── StudentsService.java             # Business contract
│   └── impl/StudentsServiceImpl.java    # Implementation
├── repository/StudentsRepository.java   # Spring Data JPA
├── mapper/StudentsMapper.java           # MapStruct entity to DTO
├── model/Students.java                  # JPA entity
├── dto/
│   ├── StudentsRequestDTO.java          # Input, with Bean Validation
│   └── StudentsResponseDTO.java         # Output
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java      # Centralized 404 handling
```

Tests live in `src/test/java/com/mx/development/jose/said/`.

---

## MCP server tools

Defined in `StudentsMcpTools` with Spring AI's `@Tool` annotation. All of them delegate to `StudentsService`.

| Tool | Description | Parameters |
|---|---|---|
| `create_student` | Creates a student | `firstName`, `lastName`, `email` (all required) |
| `get_student` | Gets a student by id | `id` (required) |
| `list_students` | Lists all students | none |
| `update_student` | Updates first name, last name or email | `id`, `firstName`, `lastName`, `email` |
| `delete_student` | Deletes a student by id | `id` (required) |
| `count_students` | Returns the total number of students | none |

- MCP endpoint (JSON-RPC over Streamable HTTP): `http://localhost:8101/mcp`
- Quick inspection over REST (no MCP client needed): `GET http://localhost:8101/api/v1/mcp/tools`

---

## REST API

Base path: `http://localhost:8101/api/v1/students`

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/api/v1/students` | Creates a student | `201 Created` with `StudentsResponseDTO` |
| `GET` | `/api/v1/students/{id}` | Gets a student by id | `200 OK` or `404` |
| `GET` | `/api/v1/students` | Lists all students | `200 OK` |
| `PUT` | `/api/v1/students/{id}` | Updates a student | `200 OK` or `404` |
| `DELETE` | `/api/v1/students/{id}` | Deletes a student | `204 No Content` |

Request body (`StudentsRequestDTO`):

```json
{
  "firstName": "Juan",
  "lastName": "Perez",
  "email": "juan.perez@example.com"
}
```

Inputs are validated with Bean Validation (`@NotNull` and `@NotBlank`). `ResourceNotFoundException` is translated to `404` by `GlobalExceptionHandler`.

Exploration tools:

- Swagger UI: http://localhost:8101/swagger-ui.html
- OpenAPI JSON: http://localhost:8101/v3/api-docs
- H2 console: http://localhost:8101/h2-console (JDBC URL `jdbc:h2:mem:studentsdb`, user `sa`, no password)

---

## Running the application

### Requirements

- JDK 21
- Maven 3.9 or later
- An **Anthropic API key**, only if you want the full flow with Claude

### 1. Set the API key (optional, recommended)

The key is read from the `ANTHROPIC_API_KEY` environment variable. It is never stored in the repository.

```bash
export ANTHROPIC_API_KEY=your_anthropic_api_key
```

Without a key, the MCP server and the REST API still work. Only the conversation with Claude fails (steps 3 to 6 of the demo), and the failure is logged with a clear message.

### 2. Start the application

```bash
mvn spring-boot:run
```

The application listens on **http://localhost:8101**. On startup, the `CommandLineRunner` runs the end-to-end demo in the console.

---

## End-to-end demo

`AppClientSample` (a `CommandLineRunner`) runs the full circuit in the console: **MCP client → MCP server → Claude → real tool → database**.

```
[Step 1] "initialize" handshake from the MCP client to the MCP server (/mcp)
[Step 2] tools/list: the MCP server exposes 6 tools
           - create_student, get_student, list_students,
             update_student, delete_student, count_students
[Step 3] Delegates to StudentCreationAgent
[Step 4] The 6 tools are available to Claude (tool calling)
[Step 5] Prompt: "Crea un nuevo estudiante llamado Juan Perez con correo juan.perez@example.com"
   >> Claude decides to call create_student with {"firstName":"Juan","lastName":"Perez",...}
      The MCP client sends tools/call → StudentsMcpTools → StudentsService → H2
[Step 6] Claude's final answer confirms the student was created and returns its ID
```

The `initialize` handshake is triggered manually, not during context startup, so that Tomcat is already listening on port 8101 before the client connects. This avoids a race condition, which is why `spring.ai.mcp.client.initialized=false` is set.

Each tool invocation is printed step by step by `LoggingToolCallback`, a decorator that wraps every `ToolCallback` without changing its behavior.

---

## Configuration

All settings are in `src/main/resources/application.properties`. Highlights:

```properties
server.port=8101

# MCP server
spring.ai.mcp.server.name=students-mcp-server
spring.ai.mcp.server.protocol=STREAMABLE
spring.ai.mcp.server.type=SYNC

# MCP client (demo, connects to the same server)
spring.ai.mcp.client.type=SYNC
spring.ai.mcp.client.initialized=false
spring.ai.mcp.client.streamable-http.connections.students-server.url=http://localhost:8101
spring.ai.mcp.client.streamable-http.connections.students-server.endpoint=/mcp

# Anthropic (Claude). The key comes from the environment and is not stored here.
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:}
spring.ai.anthropic.chat.options.model=claude-sonnet-4-6
spring.ai.anthropic.chat.options.max-tokens=1024
```

| Environment variable | Purpose | Required |
|---|---|---|
| `ANTHROPIC_API_KEY` | Anthropic API key for the Claude agent | Only for the Claude flow |

A commented MySQL block is included in the `.properties` file. Uncomment it for production use. Its password comes from the environment (`SPRING_DATASOURCE_PASSWORD`), not from the file.

---

## Technical note: why the server first exposed 0 tools

In Spring AI, **annotating methods with `@Tool` inside a `@Component` is not enough** for the MCP server to publish them. The server only registers tools that come from a `List<McpServerFeatures.SyncToolSpecification>` bean, consumed by `McpServerAutoConfiguration.mcpSyncServer(...)`.

That bean is normally generated by `ToolCallbackConverterAutoConfiguration`, which collects every `ToolCallback` and `ToolCallbackProvider` in the context and converts them. That auto-configuration would also trigger the client's `SyncMcpToolCallbackProvider` at startup, which connects to a Tomcat that is not listening yet. This is a race condition, so the auto-configuration is excluded in the `.properties` file:

```properties
spring.autoconfigure.exclude=org.springframework.ai.mcp.server.common.autoconfigure.ToolCallbackConverterAutoConfiguration
```

The side effect of that exclusion was that **no one converted the server's tools, so the server exposed 0 tools**.

**Solution** (`McpToolsConfig`): build the `List<SyncToolSpecification>` directly from the business tools only, without touching the client's provider:

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

This solves both problems: the server publishes its 6 tools, and the client race condition stays avoided.

---

## Docker

```bash
# Build and start (uses the external network scaffold-net)
./start_students.sh
# equivalent to: docker compose up --build -d
```

- **Dockerfile:** multi-stage build. The build stage uses `eclipse-temurin:21-jdk-alpine` with Maven. The runtime stage uses `eclipse-temurin:21-jre-alpine`.
- **docker-compose.yml:** publishes port `8101`. It also starts a Kafka health-check container that waits for the Kafka broker `scaffold-kafka`, and the application starts after that check.
- The `scaffold-net` network is external. Create it if it does not exist: `docker network create scaffold-net`.

---

## Testing

```bash
mvn test
```

The test suite includes `StudentsServiceTest`, which covers the service layer. The Docker build uses `-DskipTests` to speed up the image, so run the tests locally with the command above.

---

## Known limitations

- The database is in memory (H2) and is recreated on every start (`ddl-auto=create-drop`). Data does not persist between runs.
- The H2 console is enabled and accessible from other hosts (`web-allow-others=true`). Disable it outside local development.
- The Docker Compose Kafka health check refers to a broker that the application does not use.
- The demo that runs on startup calls the Anthropic API and needs a valid key to complete steps 3 to 6.

---

## Author

**Jose Said Olano Garcia**. A demonstration project for **Spring AI + MCP + Claude** integration. Feel free to use it as a reference.
