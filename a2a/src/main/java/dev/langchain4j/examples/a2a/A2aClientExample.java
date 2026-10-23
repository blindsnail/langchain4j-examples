package dev.langchain4j.examples.a2a;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.nio.file.Path;

/**
 * Run after starting the Quarkus server to invoke it through LangChain4j's A2A integration.
 */
public class A2aClientExample {

    public static void main(String[] args) {
        // Set before creating models or agents, including when launched directly from an IDE.
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        String serverUrl = System.getenv().getOrDefault("A2A_SERVER_URL", "http://localhost:8080");

        CreativeWriter creativeWriter = AgenticServices.a2aBuilder(serverUrl, CreativeWriter.class)
                .inputKeys("topic")
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .name("Style Editor")
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StyleScorer styleScorer = AgenticServices.a2aBuilder(serverUrl, StyleScorer.class)
                .outputKey("score")
                .build();

        UntypedAgent styleReviewLoop = AgenticServices
                .loopBuilder()
                .name("Review Loop")
                .subAgents(styleScorer, styleEditor)
                .maxIterations(5)
                .exitCondition(agenticScope -> agenticScope.readState("score", 0.0) >= 0.8)
                .build();

        AgentMonitor monitor = new AgentMonitor();

        StyledWriter styledWriter = AgenticServices
                .sequenceBuilder(StyledWriter.class)
                .name("Style Writer")
                .subAgents(creativeWriter, styleReviewLoop)
                .listener(monitor)
                .outputKey("story")
                .build();

        String story = styledWriter.writeStoryWithStyle("dragons and wizards", "comedy");
        System.out.println(story);

        HtmlReportGenerator.generateReport(monitor, Path.of("a2a-example.html"));
    }
}
