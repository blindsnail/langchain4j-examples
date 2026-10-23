package tutorials.voting_planner;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SentimentClassifier1 {

    @UserMessage("""
            Classify the sentiment of the following text.
            Reply with exactly one word: POSITIVE, NEGATIVE, or NEUTRAL.
            The text is: "{{text}}"
            """)
    @Agent("Classify the sentiment of a given text")
    String classify(@V("text") String text);
}

