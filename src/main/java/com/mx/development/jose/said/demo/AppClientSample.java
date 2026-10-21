package com.mx.development.jose.said.demo;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Demo end-to-end pedida por Said: crea/usa un MCP client que le habla al MCP
 * Server de Students (expuesto en /mcp), pide explícitamente la lista de
 * tools, y luego delega en {@link StudentCreationAgent} para que Claude use
 * esas tools y cree un estudiante a partir de una instrucción en lenguaje
 * natural.
 *
 * Se ejecuta automáticamente al arrancar la app (mvn spring-boot:run / IDE),
 * ya que Spring Boot invoca todos los beans CommandLineRunner justo después
 * de terminar de levantar el contexto — momento en el que el servidor
 * embebido (Tomcat, puerto 8101) ya está escuchando.
 *
 * El MCP client NO se auto-inicializa al crear el bean
 * (spring.ai.mcp.client.initialized=false); el handshake "initialize" se
 * dispara aquí, a mano, en el paso 1, precisamente para evitar una condición
 * de carrera: si Spring intentara conectarse solo durante el arranque del
 * contexto, Tomcat podría no estar escuchando todavía.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppClientSample implements CommandLineRunner {

    private final List<McpSyncClient> mcpSyncClients;
    private final StudentCreationAgent studentCreationAgent;

    @Override
    public void run(String... args) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(" DEMO: MCP Client -> MCP Server -> Claude (tool calling)");
        System.out.println("============================================================");

        for (McpSyncClient mcpClient : mcpSyncClients) {

            System.out.println();
            System.out.println("[Paso 1] Handshake \"initialize\" del MCP client contra el MCP Server (http://localhost:8101/mcp)...");
            mcpClient.initialize();
            System.out.println("    Conexión establecida y sesión MCP inicializada.");

            System.out.println();
            System.out.println("[Paso 2] Solicitando \"tools/list\" al MCP Server...");
            McpSchema.ListToolsResult toolsResult = mcpClient.listTools();
            System.out.println("    El MCP Server expone " + toolsResult.tools().size() + " tools:");
            for (McpSchema.Tool tool : toolsResult.tools()) {
                System.out.println("      - " + tool.name() + " -> " + tool.description());
            }
        }

        System.out.println();
        System.out.println("[Paso 3] Delegando en StudentCreationAgent para que Claude use esas tools...");
        String instruccion = "Crea un nuevo estudiante llamado Juan Pérez con correo juan.perez@example.com";
        //String instruccion = "Dame los datos del alumno Claudia Hernandez";

        String respuestaClaude;
        try {
            respuestaClaude = studentCreationAgent.ejecutarInstruccion(instruccion);
        } catch (Exception e) {
            log.error("No se pudo completar la conversación con Claude", e);
            System.out.println();
            System.out.println("[ERROR] No se pudo hablar con Claude: " + e.getMessage());
            System.out.println("    Verifica que la variable de entorno ANTHROPIC_API_KEY esté exportada");
            System.out.println("    y que tenga una API key válida de Anthropic.");
            return;
        }

        System.out.println();
        System.out.println("[Paso 6] Respuesta final de Claude:");
        System.out.println("    " + respuestaClaude);
        System.out.println();
        System.out.println("============================================================");
        System.out.println(" FIN DEMO");
        System.out.println("============================================================");
        System.out.println();
    }
}
