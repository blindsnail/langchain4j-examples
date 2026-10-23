package tutorials.conditional.imperative;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MedicalExpert {

    @UserMessage("""
            You are a medical expert.
            Analyze the following user request under a medical point of view and provide the best possible answer.
            The user request is {{request}}.
            """)
    @Agent(name = "Medical Expert", description = "A medical expert", outputKey = "response")
    String medical(@V("request") String request);
}