package tutorials;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.*;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.io.IOException;
import java.nio.file.Path;

public class LoopWorkflowExample {

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .name("Create Writer")
                .listener(new AgentListener() {
                    @Override
                    public void beforeAgentInvocation(AgentRequest request) {
                        System.out.println("Invoking CreativeWriter with topic: " + request.inputs().get("topic"));
                    }
                })
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .name("Style Editor")
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        StyleScorer styleScorer = AgenticServices
                .agentBuilder(StyleScorer.class)
                .name("Style Scorer")
                .chatModel(CHAT_MODEL)
                .outputKey("score")
                .build();

        UntypedAgent styleReviewLoop = AgenticServices
                .loopBuilder()
                .name("Review Loop")
                .subAgents(styleScorer, styleEditor)
                .maxIterations(5)
                .exitCondition(agenticScope -> agenticScope.readState("score", 0.0) >= 0.8)
                .build();

        AgentMonitor monitor = new AgentMonitor();

        StyledWriter styledWriter = AgenticServices
                .sequenceBuilder(StyledWriter.class)
                .name("Style Writer")
                .subAgents(creativeWriter, styleReviewLoop)
                .listener(monitor)
                .listener(new AgentListener() {
                    @Override
                    public void afterAgentInvocation(AgentResponse response) {
                        System.out.println("Agent Name: " + response.agentName());
                        if (response.agentName().equals("Style Scorer")) {
                            System.out.println("Current score: " + response.output());
                        }
                    }

                    @Override
                    public boolean inheritedBySubagents() {
                        return true;
                    }
                })
                .outputKey("story")
                .build();

        String story = styledWriter.writeStoryWithStyle("dragons and wizards", "comedy");
        System.out.println(story);

        MonitoredExecution execution = monitor.successfulExecutions().get(0);
        System.out.println(execution);

        HtmlReportGenerator.generateReport(monitor, Path.of("review-loop.html"));
    }
}