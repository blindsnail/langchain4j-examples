package tutorials.p2p;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.p2p.P2PPlanner;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;

public class P2PAgentExample {
    public static void main(String[] args) {
        ArxivCrawler arxivCrawler = new ArxivCrawler();

        LiteratureAgent literatureAgent = AgenticServices.agentBuilder(LiteratureAgent.class)
                .chatModel(baseModel())
                .tools(arxivCrawler)
                .outputKey("researchFindings")
                .build();
        HypothesisAgent hypothesisAgent = AgenticServices.agentBuilder(HypothesisAgent.class)
                .chatModel(baseModel())
                .tools(arxivCrawler)
                .outputKey("hypothesis")
                .build();
        CriticAgent criticAgent = AgenticServices.agentBuilder(CriticAgent.class)
                .chatModel(baseModel())
                .tools(arxivCrawler)
                .outputKey("critique")
                .build();
        ValidationAgent validationAgent = AgenticServices.agentBuilder(ValidationAgent.class)
                .chatModel(baseModel())
                .tools(arxivCrawler)
                .outputKey("hypothesis")
                .build();
        ScorerAgent scorerAgent = AgenticServices.agentBuilder(ScorerAgent.class)
                .chatModel(baseModel())
                .tools(arxivCrawler)
                .outputKey("score")
                .build();

        P2PPlanner planner = new P2PPlanner(10, agenticScope -> {
            if (!agenticScope.hasState("score")) {
                return false;
            }
            double score = agenticScope.readState("score", 0.0);
            System.out.println("Current hypothesis score: " + score);
            return score >= 0.85;
        });

        AgentMonitor monitor = new AgentMonitor();

        ResearchAgent researcher = AgenticServices.plannerBuilder(ResearchAgent.class)
                .subAgents(literatureAgent, hypothesisAgent, criticAgent, validationAgent, scorerAgent)
                .outputKey("hypothesis")
                .planner(() -> planner)
                .listener(monitor)
                .build();

        String hypothesis = researcher.research("black holes");
        System.out.println(hypothesis);

        HtmlReportGenerator.generateReport(monitor, Path.of("p2p-agent-example.html"));
    }

    private static ChatModel baseModel() {
        return ChatModelProvider.createChatModel();
    }
}
