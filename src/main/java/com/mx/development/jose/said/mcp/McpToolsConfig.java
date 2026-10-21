package com.mx.development.jose.said.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Expone las {@link org.springframework.ai.tool.annotation.Tool @Tool} de
 * {@link StudentsMcpTools} en el MCP Server.
 *
 * <p>Contexto: {@code application.properties} excluye
 * {@code ToolCallbackConverterAutoConfiguration} para evitar que, durante el
 * arranque del contexto, Spring AI agregue el {@code SyncMcpToolCallbackProvider}
 * del <em>cliente</em> (usado por {@code StudentCreationAgent}) e invoque su
 * {@code getToolCallbacks()} contra un Tomcat que todavía no escucha — la race
 * condition que comentaba el .properties.</p>
 *
 * <p>El efecto colateral era que ese mismo auto-config es también el que
 * convierte las {@code ToolCallback} del <em>server</em> en
 * {@link McpServerFeatures.SyncToolSpecification}. Al excluirlo, el server
 * quedaba con 0 tools.</p>
 *
 * <p>Aquí producimos directamente el {@code List<SyncToolSpecification>} (que
 * {@code McpServerAutoConfiguration.mcpSyncServer(...)} consume) SOLO a partir
 * de nuestras tools de negocio, sin tocar el provider del cliente: se resuelven
 * ambos problemas a la vez.</p>
 */
@Configuration
public class McpToolsConfig {

    @Bean
    public List<McpServerFeatures.SyncToolSpecification> studentsToolSpecifications(StudentsMcpTools studentsMcpTools) {
        ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(studentsMcpTools)
                .build()
                .getToolCallbacks();
        return McpToolUtils.toSyncToolSpecifications(callbacks);
    }
}
