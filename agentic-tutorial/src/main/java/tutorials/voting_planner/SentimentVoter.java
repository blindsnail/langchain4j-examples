package tutorials.voting_planner;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.V;

public interface SentimentVoter {

    @Agent("Classify customer feedback by aggregating independent sentiment votes")
    String classify(@V("text") String text);
}
