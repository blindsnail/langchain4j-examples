package tutorials.bdi;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.bdi.BDIPlanner;
import dev.langchain4j.agentic.patterns.bdi.Desire;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates a trading workflow whose intentions are selected dynamically
 * from the current market beliefs.
 */
public class BdiPatternExample {

    public static void main(String[] args) {
        ChatModel model = ChatModelProvider.createChatModel();

        MarketAnalysisAgent marketAnalysis = AgenticServices.agentBuilder(MarketAnalysisAgent.class)
                .chatModel(model).build();

        MarketRecommendationAgent recommendation = AgenticServices.agentBuilder(MarketRecommendationAgent.class)
                .chatModel(model).build();

        RebalancingAgent rebalancing = AgenticServices.agentBuilder(RebalancingAgent.class)
                .chatModel(model).build();

        HedgingAgent hedging = AgenticServices.agentBuilder(HedgingAgent.class)
                .chatModel(model).build();

        LiquidityAgent liquidity = AgenticServices.agentBuilder(LiquidityAgent.class)
                .chatModel(model).build();

        AgentMonitor monitor = new AgentMonitor();

        TradingSystem tradingSystem = AgenticServices.plannerBuilder(TradingSystem.class)
                .subAgents(marketAnalysis, recommendation, new HedgingStrategyDefaulter(),
                        rebalancing, hedging, liquidity)
                .planner(() -> new BDIPlanner(List.of(
                        Desire.of("analyze market", 1,
                                "marketData", "recommendation",
                                MarketAnalysisAgent.class, MarketRecommendationAgent.class),
                        Desire.of("hedge risks", 2,
                                scope -> scope.hasState("recommendation")
                                        && Set.of(MarketRecommendation.SELL, MarketRecommendation.STRONG_SELL)
                                        .contains(scope.readState("recommendation")),
                                scope -> scope.hasState("hedgingStrategy"),
                                HedgingAgent.class),
                        Desire.of("rebalance portfolio", 1,
                                "recommendation", "rebalancingPlan",
                                HedgingStrategyDefaulter.class, RebalancingAgent.class),
                        Desire.of("maintain liquidity", 1,
                                "portfolio", "liquidityAssessment",
                                LiquidityAgent.class)
                )))
                .listener(monitor)
                .outputKey("rebalancingPlan")
                .build();

        String plan = tradingSystem.trade(
                "Technology shares are volatile after the latest earnings reports.",
                "A diversified portfolio of technology and healthcare stocks.");
        System.out.println(plan);

        HtmlReportGenerator.generateReport(monitor, Path.of("bdi-agentic-pattern.html"));
    }

}
