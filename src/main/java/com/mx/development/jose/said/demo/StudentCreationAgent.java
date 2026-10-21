package com.mx.development.jose.said.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Clase propuesta por Claude (a pedido de Said) para hablarle a Claude.
 *
 * Toma las tools MCP ya descubiertas por el MCP client (a través de
 * {@link SyncMcpToolCallbackProvider}, que internamente usa el mismo
 * {@code McpSyncClient} que AppClientSample inicializó), las envuelve con
 * {@link LoggingToolCallback} para que cada invocación quede impresa, y le
 * pide a Claude (Anthropic, vía Spring AI tool calling) que cumpla una
 * instrucción en lenguaje natural usando esas tools si lo considera necesario.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StudentCreationAgent {

    private final ChatModel chatModel;
    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    /**
     * Envía la instrucción a Claude junto con las tools MCP disponibles y
     * devuelve la respuesta final en lenguaje natural.
     */
    public String ejecutarInstruccion(String instruccion) {
        ToolCallback[] toolCallbacks = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(LoggingToolCallback::new)
                .toArray(ToolCallback[]::new);

        System.out.println("[Paso 4] Tools MCP disponibles para Claude (tool calling): " + toolCallbacks.length);
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println("      - " + toolCallback.getToolDefinition().name());
        }

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbacks)
                .build();

        System.out.println();
        System.out.println("[Paso 5] Enviando prompt a Claude:");
        System.out.println("    \"" + instruccion + "\"");
        System.out.println("    (Claude decide si necesita alguna tool MCP para responder)");

        String respuesta = chatClient.prompt()
                .user(instruccion)
                .call()
                .content();

        log.info("Respuesta final de Claude: {}", respuesta);
        return respuesta;
    }
}
