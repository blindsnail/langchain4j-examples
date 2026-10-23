package tutorials.conditional.imperative;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;

public class ConditionalWorkflowExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        CategoryRouter routerAgent = AgenticServices.agentBuilder(CategoryRouter.class).chatModel(CHAT_MODEL).build();

        MedicalExpert medicalExpert = AgenticServices.agentBuilder(MedicalExpert.class).chatModel(CHAT_MODEL).build();

        LegalExpert legalExpert = AgenticServices.agentBuilder(LegalExpert.class).chatModel(CHAT_MODEL).build();

        TechnicalExpert technicalExpert = AgenticServices.agentBuilder(TechnicalExpert.class).chatModel(CHAT_MODEL).build();

        UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
                .subAgents(
                        agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.MEDICAL,
                        medicalExpert
                )
                .subAgents(
                        agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL,
                        legalExpert
                )
                .subAgents(
                        agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.TECHNICAL,
                        technicalExpert
                )
                .build();

        ExpertRouterAgent expertRouterAgent = AgenticServices
                .sequenceBuilder(ExpertRouterAgent.class)
                .subAgents(routerAgent, expertsAgent)
                .build();

        String response = expertRouterAgent.ask("I broke my leg what should I do");
        System.out.println(response);

        AgentMonitor monitor = expertRouterAgent.agentMonitor();
        HtmlReportGenerator.generateReport(monitor, Path.of("export-router.html"));
    }
}
