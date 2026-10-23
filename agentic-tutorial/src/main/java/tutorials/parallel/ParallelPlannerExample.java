package tutorials.parallel;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParallelPlannerExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        FoodExpert foodExpert = AgenticServices
                .agentBuilder(FoodExpert.class)
                .chatModel(CHAT_MODEL)
                .outputKey("meals")
                .build();

        MovieExpert movieExpert = AgenticServices
                .agentBuilder(MovieExpert.class)
                .chatModel(CHAT_MODEL)
                .outputKey("movies")
                .build();

        AgentMonitor monitor = new AgentMonitor();

        EveningPlannerAgent eveningPlannerAgent = AgenticServices
                .plannerBuilder(EveningPlannerAgent.class)
                .subAgents(foodExpert, movieExpert)
                .planner(ParallelPlanner::new)
                .listener(monitor)
                .outputKey("plans")
                .output(agenticScope -> {
                    List<String> movies = agenticScope.readState("movies", List.of());
                    List<String> meals = agenticScope.readState("meals", List.of());

                    List<EveningPlan> moviesAndMeals = new ArrayList<>();
                    for (int i = 0; i < movies.size(); i++) {
                        if (i >= meals.size()) {
                            break;
                        }
                        moviesAndMeals.add(new EveningPlan(movies.get(i), meals.get(i)));
                    }
                    return moviesAndMeals;
                })
                .build();

        List<EveningPlan> plans = eveningPlannerAgent.plan("romantic");
        plans.forEach(System.out::println);

        HtmlReportGenerator.generateReport(monitor, Path.of("parallel-planner.html"));
    }
}
