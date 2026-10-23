package tutorials.conditional.declarative;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CategoryRouter {

    @UserMessage("""
            Analyze the following user request and categorize it as 'legal', 'medical' or 'technical'.
            In case the request doesn't belong to any of those categories categorize it as 'unknown'.
            Reply with only one of those words and nothing else.
            The user request is: '{{request}}'.
            """)
    @Agent(value = "Categorizes a user request", outputKey = "category")
    RequestCategory classify(@V("request") String request);

    @ChatModelSupplier
    static ChatModel chatModel() {
        return ConditionalWorkflowExample.CHAT_MODEL;
    }
}