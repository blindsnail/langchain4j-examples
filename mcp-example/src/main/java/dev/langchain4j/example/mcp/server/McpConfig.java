package dev.langchain4j.example.mcp.server;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
public class McpConfig {
    @Bean
    McpJsonMapper mcpJsonMapper() {
        return McpJsonDefaults.getMapper();
    }

    @Bean
    WebMvcStreamableServerTransportProvider transportProvider(McpJsonMapper jsonMapper) {
        return WebMvcStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint("/mcp")
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> mcpRouterFunction(WebMvcStreamableServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }

    @Bean(destroyMethod = "close")
    McpSyncServer mcpSyncServer(WebMvcStreamableServerTransportProvider transportProvider) {
        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "a", Map.of("type", "number", "description", "First number"),
                        "b", Map.of("type", "number", "description", "Second number")
                ),
                "required", List.of("a", "b"),
                "additionalProperties", false
        );

        McpSchema.Tool addTool = McpSchema.Tool.builder("add", inputSchema)
                .description("Add two numbers and return their sum")
                .build();

        return McpServer.sync(transportProvider)
                .serverInfo("langchain4j-example-server", "1.0.0")
                .instructions("Use the add tool to calculate the sum of two numbers.")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                .toolCall(addTool, (exchange, request) -> {
                    Number a = (Number) request.arguments().get("a");
                    Number b = (Number) request.arguments().get("b");
                    double result = a.doubleValue() + b.doubleValue();

                    return McpSchema.CallToolResult.builder()
                            .content(List.of(new McpSchema.TextContent("Result: " + result)))
                            .build();
                })
                .build();
    }
}
