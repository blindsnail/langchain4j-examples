package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScope;

public class HedgingStrategyDefaulter {
    @Agent(outputKey = "hedgingStrategy")
    public String defaultHedging(AgenticScope scope) {
        return scope.hasState("hedgingStrategy") ? (String) scope.readState("hedgingStrategy") : "None";
    }
}
