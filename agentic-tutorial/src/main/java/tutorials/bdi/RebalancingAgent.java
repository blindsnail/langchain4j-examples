package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RebalancingAgent {
    @UserMessage("Suggest rebalancing based on: {{marketAnalysis}} Hedging strategy: {{hedgingStrategy}} Portfolio: {{portfolio}}")
    @Agent(value = "Rebalance portfolio", outputKey = "rebalancingPlan")
    String rebalance(@V("marketAnalysis") String marketAnalysis,
                     @V("hedgingStrategy") String hedgingStrategy,
                     @V("portfolio") String portfolio);
}
