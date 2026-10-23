package tutorials.conditional.declarative;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;

public class ConditionalWorkflowExample {
    static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        ExpertRouterAgent expertRouterAgent = AgenticServices.createAgenticSystem(ExpertRouterAgent.class);

        String response = expertRouterAgent.ask("I broke my leg what should I do");
        System.out.println(response);

        AgentMonitor monitor = expertRouterAgent.agentMonitor();
        HtmlReportGenerator.generateReport(monitor, Path.of("declarative-export-router.html"));
    }
}
