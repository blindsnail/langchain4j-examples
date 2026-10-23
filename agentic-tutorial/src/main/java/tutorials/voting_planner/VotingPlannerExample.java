package tutorials.voting_planner;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.p2p.P2PPlanner;
import dev.langchain4j.agentic.patterns.voting.VotingPlanner;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.p2p.*;
import util.ChatModelProvider;

import java.nio.file.Path;

public class VotingPlannerExample {
    public static void main(String[] args) {
        SentimentClassifier1 c1 = AgenticServices.agentBuilder(SentimentClassifier1.class)
                .chatModel(baseModel())
                .outputKey("vote1")
                .build();

        SentimentClassifier2 c2 = AgenticServices.agentBuilder(SentimentClassifier2.class)
                .chatModel(baseModel())
                .outputKey("vote2")
                .build();

        SentimentClassifier3 c3 = AgenticServices.agentBuilder(SentimentClassifier3.class)
                .chatModel(baseModel())
                .outputKey("vote3")
                .build();

        AgentMonitor monitor = new AgentMonitor();

        SentimentVoter voter = AgenticServices.plannerBuilder(SentimentVoter.class)
                .subAgents(c1, c2, c3)
                .listener(monitor)
                .outputKey("classification")
                .planner(VotingPlanner::new)
                .build();

        String classification = voter.classify("I absolutely love this product! It exceeded all my expectations.");
        System.out.println("classification: " + classification);

        HtmlReportGenerator.generateReport(monitor, Path.of("voting-agent.html"));
    }

    private static ChatModel baseModel() {
        return ChatModelProvider.createChatModel();
    }
}
