package com.mx.development.said.students.demo;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Decora un {@link ToolCallback} real (una tool MCP ya convertida a tool de
 * Spring AI) únicamente para imprimir, paso a paso, el momento exacto en que
 * Claude decide invocarla: con qué argumentos, y qué responde el MCP Server.
 *
 * No cambia el comportamiento de la tool — solo lo hace visible en consola.
 */
public class LoggingToolCallback implements ToolCallback {

    private final ToolCallback delegate;

    public LoggingToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        String toolName = delegate.getToolDefinition().name();

        System.out.println();
        System.out.println("    >> Claude decidió invocar la tool MCP \"" + toolName + "\"");
        System.out.println("       Argumentos (JSON): " + toolInput);
        System.out.println("       El MCP client envía \"tools/call\" al MCP Server...");

        String result = delegate.call(toolInput);

        System.out.println("       <- El MCP Server respondió: " + result);

        return result;
    }
}
