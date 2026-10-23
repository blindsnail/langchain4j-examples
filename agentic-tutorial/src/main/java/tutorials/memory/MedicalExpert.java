package tutorials.memory;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MedicalExpert {

    @UserMessage("""
            You are a medical expert.
            Analyze the following user request under a medical point of view and provide the best possible answer.
            The user request is {{request}}.
            """)
    @Agent(
            name = "medical",
            description = "A medical expert, answering questions about health, injuries and treatments",
            outputKey = "response"
    )
    String medical(@MemoryId String memoryId, @V("request") String request);
}