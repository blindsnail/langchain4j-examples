package tutorials.conditional.declarative;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TechnicalExpert {

    @UserMessage("""
            You are a technical expert.
            Analyze the following user request under a technical point of view and provide the best possible answer.
            The user request is {{request}}.
            """)
    @Agent(name = "Technical Expert", description = "A technical expert", outputKey = "response")
    String technical(@V("request") String request);

    @ChatModelSupplier
    static ChatModel chatModel() {
        return ConditionalWorkflowExample.CHAT_MODEL;
    }
}
