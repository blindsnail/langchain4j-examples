package dev.langchain4j.examples.a2a_server;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;

@ApplicationScoped
public class AgentExecutorProducer {

    @Produces
    @ApplicationScoped
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Produces
    @ApplicationScoped
    public CreativeWriter creativeWriter(ChatModel chatModel) {
        return AgenticServices.agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .build();
    }

    @Produces
    public AgentExecutor creativeWriterExecutor(CreativeWriter creativeWriter) {
        return new CreativeWriterAgentExecutor(creativeWriter);
    }
}
