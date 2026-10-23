package tutorials.parallel;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.util.List;

public class DeclarativeParallelWorkflowExample {
    static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        DeclarativeEveningPlannerAgent eveningPlannerAgent =
                AgenticServices.createAgenticSystem(DeclarativeEveningPlannerAgent.class);

        List<EveningPlan> plans = eveningPlannerAgent.plan("romantic");
        plans.forEach(System.out::println);
    }
}
