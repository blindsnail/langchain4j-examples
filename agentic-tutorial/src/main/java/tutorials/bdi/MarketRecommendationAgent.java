package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MarketRecommendationAgent {
    @UserMessage("Based on the market analysis, provide a trading recommendation. Market analysis: {{marketAnalysis}}")
    @Agent(value = "Provide a trading recommendation", outputKey = "recommendation")
    MarketRecommendation recommend(@V("marketAnalysis") String marketAnalysis);
}
