package tutorials.decision_router_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.Map;

public interface ResponseSynthesizer {

    @UserMessage("""
        Merge the answers that different experts gave to the same user request into a single answer.
        The user request is: {{request}}
        The answers of the experts, keyed by expert, are: {{responses}}
        """)
    @Agent(description = "Merges the answers of several experts into one", outputKey = "answer")
    String synthesize(@V("request") String request, @V("responses") Map<String, String> responses);
}
