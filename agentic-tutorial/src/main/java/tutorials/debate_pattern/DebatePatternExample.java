package tutorials.debate_pattern;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.debate.DebatePlanner;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;

public class DebatePatternExample {
    public static void main(String[] args) {
        UtilitarianDebater d1 = AgenticServices.agentBuilder(UtilitarianDebater.class)
                .chatModel(baseModel())
                .outputKey("utilitarian")
                .build();

        DeontologicalDebater d2 = AgenticServices.agentBuilder(DeontologicalDebater.class)
                .chatModel(baseModel())
                .outputKey("deontological")
                .build();

        PragmatistDebater d3 = AgenticServices.agentBuilder(PragmatistDebater.class)
                .chatModel(baseModel())
                .outputKey("pragmatist")
                .build();

        EthicsJudge judge = AgenticServices.agentBuilder(EthicsJudge.class)
                .chatModel(baseModel())
                .outputKey("verdict")
                .build();

        AgentMonitor monitor = new AgentMonitor();

        EthicsPanel panel = AgenticServices.plannerBuilder(EthicsPanel.class)
                .subAgents(d1, d2, d3, judge)
                .listener(monitor)
                .outputKey("verdict")
                .planner(() -> new DebatePlanner(100))
                .build();

        String result = panel.debate(
                "Is it ethical to use AI-generated art in commercial products without crediting the AI tool?"
        );
        System.out.println(result);

        HtmlReportGenerator.generateReport(monitor, Path.of("debate-pattern.html"));
    }

    private static ChatModel baseModel() {
        return ChatModelProvider.createChatModel();
    }
}
