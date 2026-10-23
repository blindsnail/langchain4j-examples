package tutorials.voting_planner;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SentimentClassifier2 {

    @UserMessage("""
            You are a sentiment analysis expert.
            Analyze the emotional tone of the following text and classify it.
            Reply with exactly one word: POSITIVE, NEGATIVE, or NEUTRAL.
            The text is: "{{text}}"
            """)
    @Agent("Analyze the emotional tone of a given text")
    String classify(@V("text") String text);
}
