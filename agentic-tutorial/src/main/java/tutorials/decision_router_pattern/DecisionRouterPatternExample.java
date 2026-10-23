package tutorials.decision_router_pattern;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.patterns.decisionrouter.DecisionRouterPlanner;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import util.ChatModelProvider;

import java.nio.file.Path;
import java.util.Map;

public class DecisionRouterPatternExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        MedicalExpert medicalExpert = AgenticServices.agentBuilder(MedicalExpert.class)
                .chatModel(CHAT_MODEL)
                .build();

        LegalExpert legalExpert = AgenticServices.agentBuilder(LegalExpert.class)
                .chatModel(CHAT_MODEL)
                .build();

        TechnicalExpert technicalExpert = AgenticServices.agentBuilder(TechnicalExpert.class)
                .chatModel(CHAT_MODEL)
                .build();

        DecisionModel decisionModel = TypeSafeDecisionModel.builder()
                .apiKey("apikey_2127599576886d544ac8a132ba1593afbf84_e68362f0723b68ca92046121f7c419736361e063a2b3338aaad784da660adb99")
                .modelName("jev-1.13.0")
                .logRequests(true)
                .logResponses(true)
                .build();

        MultiExpertRouterAgent multiExpertRouterAgent = AgenticServices.plannerBuilder(MultiExpertRouterAgent.class)
                .subAgents(medicalExpert, legalExpert, technicalExpert)
                .outputKey("responses")
                .planner(() -> new DecisionRouterPlanner(decisionModel, 0.5))
                .build();

        ResponseSynthesizer responseSynthesizer = AgenticServices.agentBuilder(ResponseSynthesizer.class)
                .chatModel(CHAT_MODEL)
                .build();

        AgentMonitor monitor = new AgentMonitor();

        ExpertPipeline pipeline = AgenticServices.sequenceBuilder(ExpertPipeline.class)
                .subAgents(multiExpertRouterAgent, responseSynthesizer)
                .listener(monitor)
                .outputKey("answer")
                .build();

        String answer = pipeline.process(
                "I broke my leg in a car accident caused by another driver: " +
                        "how should I take care of my leg, and can I sue the driver for damages?");

        System.out.println(answer);

        HtmlReportGenerator.generateReport(monitor, Path.of("decision-router-pattern.html"));
    }
}
