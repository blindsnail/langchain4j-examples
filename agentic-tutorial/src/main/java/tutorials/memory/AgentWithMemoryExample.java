package tutorials.memory;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.decisionrouter.DecisionRouterPlanner;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import util.ChatModelProvider;

import java.nio.file.Path;

public class AgentWithMemoryExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {

        MedicalExpert medicalExpert = AgenticServices.agentBuilder(MedicalExpert.class)
                .chatModel(CHAT_MODEL)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .summarizedContext("legal", "technical")
                .build();

        LegalExpert legalExpert = AgenticServices.agentBuilder(LegalExpert.class)
                .chatModel(CHAT_MODEL)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .summarizedContext("medical", "technical")
                .build();

        TechnicalExpert technicalExpert = AgenticServices.agentBuilder(TechnicalExpert.class)
                .chatModel(CHAT_MODEL)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .summarizedContext("medical", "legal")
                .build();

        DecisionModel decisionModel = TypeSafeDecisionModel.builder()
                .apiKey("apikey_2127599576886d544ac8a132ba1593afbf84_e68362f0723b68ca92046121f7c419736361e063a2b3338aaad784da660adb99")
                .modelName("jev-1.13.0")
                .logRequests(true)
                .logResponses(true)
                .build();

        AgentMonitor monitor = new AgentMonitor();

        ExpertRouterAgent expertRouterAgent = AgenticServices.plannerBuilder(ExpertRouterAgent.class)
                .subAgents(medicalExpert, legalExpert, technicalExpert)
                .outputKey("response")
                .listener(monitor)
                .planner(() -> new DecisionRouterPlanner(decisionModel))
                .build();

        String response = expertRouterAgent.ask("1", "I broke my leg what should I do");
        System.out.println(response);

        response = expertRouterAgent.ask("1", "Should I sue my neighbor who caused this damage?");
        System.out.println(response);

        HtmlReportGenerator.generateReport(monitor, Path.of("agent-with-memory.html"));
    }
}
