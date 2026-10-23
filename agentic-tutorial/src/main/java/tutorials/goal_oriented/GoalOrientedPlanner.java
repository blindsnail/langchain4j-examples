package tutorials.goal_oriented;

import dev.langchain4j.agentic.patterns.goap.GoalOrientedSearchGraph;
import dev.langchain4j.agentic.planner.*;

import java.util.List;

public class GoalOrientedPlanner implements Planner {
    private String goal;

    private GoalOrientedSearchGraph graph;
    private List<AgentInstance> path;

    private int agentCursor = 0;

    @Override
    public void init(InitPlanningContext initPlanningContext) {
        this.goal = initPlanningContext.plannerAgent().outputKey();
        this.graph = new GoalOrientedSearchGraph(initPlanningContext.subagents());
        System.out.println("Goal: " + goal);
        System.out.println("Graph: " + graph);
    }

    @Override
    public Action firstAction(PlanningContext planningContext) {
        path = graph.search(planningContext.agenticScope().state().keySet(), goal);
        System.out.println("path: " + path);
        if (path.isEmpty()) {
            throw new IllegalStateException("No path found for goal: " + goal);
        }
        return call(path.get(agentCursor++));
    }

    @Override
    public Action nextAction(PlanningContext planningContext) {
        return agentCursor >= path.size() ? done() : call(path.get(agentCursor++));
    }
}