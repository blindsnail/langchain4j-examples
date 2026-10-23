package tutorials.pure_agentic;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.math.BigDecimal;
import java.nio.file.Path;

public class PureAgenticExample {
    static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        BankTool bankTool = new BankTool();
        bankTool.createAccount("Mario", new BigDecimal("1000"));
        bankTool.createAccount("Georgios", new BigDecimal("1000"));

        WithdrawAgent withdrawAgent = AgenticServices
                .agentBuilder(WithdrawAgent.class)
                .chatModel(CHAT_MODEL)
                .tools(bankTool)
                .build();

        CreditAgent creditAgent = AgenticServices
                .agentBuilder(CreditAgent.class)
                .chatModel(CHAT_MODEL)
                .tools(bankTool)
                .build();

        AgentMonitor monitor = new AgentMonitor();

        SupervisorAgent bankSupervisor = AgenticServices
                .supervisorBuilder()
                .listener(monitor)
                .chatModel(CHAT_MODEL)
                .subAgents(withdrawAgent, creditAgent, new ExchangeAgent())
                .responseStrategy(SupervisorResponseStrategy.SCORED)
                .build();

        bankSupervisor.invoke("Transfer 100 EUR from Mario's account to Georgios' one");

        System.out.println("Mario's Balance: " + bankTool.getBalance("Mario"));
        System.out.println("Georgios's Balance: " + bankTool.getBalance("Georgios"));

        HtmlReportGenerator.generateReport(monitor, Path.of("pure-agentic-example.html"));
    }
}
