package tutorials.voting_planner;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SentimentClassifier3 {

    @UserMessage("""
            You are a customer feedback analyst.
            Determine whether the following feedback is positive, negative, or neutral.
            Reply with exactly one word: POSITIVE, NEGATIVE, or NEUTRAL.
            The text is: "{{text}}"
            """)
    @Agent("Determine the sentiment of customer feedback")
    String classify(@V("text") String text);
}
