package com.mx.development.jose.said.controller;

import com.mx.development.jose.said.mcp.StudentsMcpTools;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint de solo lectura para inspeccionar, vía REST/Swagger, qué tools MCP
 * expone {@link StudentsMcpTools} — sin tener que hablar JSON-RPC/handshake
 * del protocolo MCP contra /mcp. Útil solo para debug/documentación.
 */
@Tag(name = "MCP Debug", description = "Inspección de las tools expuestas por el MCP Server")
@RestController
@RequestMapping("/api/v1/mcp")
public class StudentsMcpDebugController {

    @Operation(summary = "Lista las tools MCP registradas en StudentsMcpTools")
    @GetMapping("/tools")
    public ResponseEntity<List<McpToolInfo>> listTools() {
        List<McpToolInfo> tools = new ArrayList<>();

        for (Method method : StudentsMcpTools.class.getDeclaredMethods()) {
            Tool mcpTool = method.getAnnotation(Tool.class);
            if (mcpTool == null) {
                continue;
            }

            List<McpToolParamInfo> params = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                ToolParam mcpToolParam = parameter.getAnnotation(ToolParam.class);
                params.add(new McpToolParamInfo(
                        parameter.getName(),
                        parameter.getType().getSimpleName(),
                        mcpToolParam != null ? mcpToolParam.description() : "",
                        mcpToolParam != null && mcpToolParam.required()
                ));
            }

            String name = !mcpTool.name().isBlank() ? mcpTool.name() : method.getName();
            tools.add(new McpToolInfo(name, mcpTool.description(), params));
        }

        return ResponseEntity.ok(tools);
    }

    public record McpToolParamInfo(String name, String type, String description, boolean required) {
    }

    public record McpToolInfo(String name, String description, List<McpToolParamInfo> parameters) {
    }
}
