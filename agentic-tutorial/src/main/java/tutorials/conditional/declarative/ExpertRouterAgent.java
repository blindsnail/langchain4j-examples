package tutorials.conditional.declarative;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.service.V;

public interface ExpertRouterAgent extends MonitoredAgent {

    @SequenceAgent(outputKey = "response", subAgents = {CategoryRouter.class, ExpertsAgent.class})
    String ask(@V("request") String request);
}