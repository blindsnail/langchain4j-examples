package tutorials.bdi;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MarketAnalysisAgent {
    @UserMessage("Analyze the market data and portfolio. Market: {{marketData}} Portfolio: {{portfolio}}")
    @Agent(value = "Analyze market conditions", outputKey = "marketAnalysis")
    String analyzeMarket(@V("marketData") String marketData, @V("portfolio") String portfolio);
}
