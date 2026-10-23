package tutorials.decision_router_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface ExpertPipeline {

    @Agent
    String process(@V("request") String request);
}