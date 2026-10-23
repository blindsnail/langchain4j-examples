package tutorials.memory;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface ExpertRouterAgent extends MonitoredAgent {

    @Agent(name = "Expert Router", outputKey = "response")
    String ask(@MemoryId String memoryId, @V("request") String request);
}