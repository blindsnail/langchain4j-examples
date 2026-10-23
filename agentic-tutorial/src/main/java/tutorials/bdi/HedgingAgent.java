package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface HedgingAgent {
    @UserMessage("Recommend hedging strategies based on: {{marketAnalysis}}")
    @Agent(value = "Hedge against risks", outputKey = "hedgingStrategy")
    String hedge(@V("marketAnalysis") String marketAnalysis);
}
