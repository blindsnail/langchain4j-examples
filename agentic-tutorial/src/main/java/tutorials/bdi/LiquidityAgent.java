package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface LiquidityAgent {
    @UserMessage("Assess liquidity for portfolio: {{portfolio}}")
    @Agent(value = "Maintain liquidity", outputKey = "liquidityAssessment")
    String assessLiquidity(@V("portfolio") String portfolio);
}
