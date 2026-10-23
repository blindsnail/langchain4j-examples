package dev.langchain4j.example.mcp.client;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import java.time.Duration;
import java.util.List;

public class WeatherToolsExample {
    public static void main(String[] args) {
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        McpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url("http://localhost:8080/mcp")
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        List<ToolSpecification> tools = mcpClient.listTools();
        System.out.println("tools: " + tools);

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        Bot bot = AiServices.builder(Bot.class)
                .chatModel(model)
                .toolProvider(toolProvider)
                .build();

        String response = bot.chat("根据经纬度查询天气预报，经度：-122.3321，纬度：47.6062");
        System.out.println(response);

        response = bot.chat("查询加利福尼亚地区的警告");
        System.out.println(response);
    }
}
