package tutorials.decision_router_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface LegalExpert {

    @UserMessage("""
            You are a legal expert.
            Analyze the following user request under a legal point of view and provide the best possible answer.
            The user request is {{request}}.
            """)
    @Agent(
            name = "Legal Expert",
            description = "A legal expert, answering questions about laws, rights, contracts and lawsuits",
            outputKey = "response"
    )
    String legal(@V("request") String request);
}