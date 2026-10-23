package tutorials.parallel;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

import java.util.List;

public interface EveningPlannerAgent {
    @Agent
    List<EveningPlan> plan(@V("mood") String mood);
}
