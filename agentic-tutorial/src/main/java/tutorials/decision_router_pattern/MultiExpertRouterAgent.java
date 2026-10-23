package tutorials.decision_router_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

import java.util.Map;

public interface MultiExpertRouterAgent {

    @Agent
    Map<String, String> ask(@V("request") String request);
}