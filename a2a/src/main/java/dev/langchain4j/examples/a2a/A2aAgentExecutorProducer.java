package dev.langchain4j.examples.a2a;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;

@ApplicationScoped
public class A2aAgentExecutorProducer {

    @Produces
    @ApplicationScoped
    ChatModel chatModel() {
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
    CreativeWriter creativeWriter(ChatModel chatModel) {
        return AgenticServices.agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .build();
    }

    @Produces
    @ApplicationScoped
    StyleScorer styleScorer(ChatModel chatModel) {
        return AgenticServices.agentBuilder(StyleScorer.class)
                .chatModel(chatModel)
                .build();
    }

    @Produces
    AgentExecutor agentExecutor(CreativeWriter creativeWriter, StyleScorer styleScorer) {
        return new StoryServicesAgentExecutor(creativeWriter, styleScorer);
    }
}
