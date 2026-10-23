package dev.langchain4j.example.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.Map;

/**
 * Connects to the MCP server in this module, discovers its tools, and invokes {@code add}.
 * Start {@code ServingWebContentApplication} before running this example.
 */
public class McpSdkClientExample {

    private static final String SERVER_URL = System.getProperty("mcp.server.url", "http://localhost:8080");

    public static void main(String[] args) {
        var transport = HttpClientStreamableHttpTransport.builder(SERVER_URL)
                .endpoint("/mcp")
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        try (McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .build()) {
            McpSchema.InitializeResult initialization = client.initialize();
            System.out.printf("Connected to %s %s (protocol %s)%n",
                    initialization.serverInfo().name(),
                    initialization.serverInfo().version(),
                    initialization.protocolVersion());

            McpSchema.ListToolsResult availableTools = client.listTools();
            availableTools.tools().forEach(tool ->
                    System.out.printf("Tool: %s - %s%n", tool.name(), tool.description()));

            McpSchema.CallToolResult result = client.callTool(
                    McpSchema.CallToolRequest.builder("add")
                            .arguments(Map.of("a", 5, "b", 12))
                            .build());

            if (Boolean.TRUE.equals(result.isError())) {
                throw new IllegalStateException("MCP tool call failed: " + result.content());
            }

            result.content().forEach(content -> {
                if (content instanceof McpSchema.TextContent textContent) {
                    System.out.println(textContent.text());
                } else {
                    System.out.println(content);
                }
            });
        }
    }
}
