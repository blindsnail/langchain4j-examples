package tutorials.debate_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

public interface PragmatistDebater {

    @UserMessage("""
            You are a pragmatist debater.
            Consider the following question and argue based on practical consequences and real-world outcomes.
            If previous debate context is provided, consider the other debaters' arguments and refine your position.
            Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
            Question: {{question}}
            Previous debate context: {{debateContext}}
            """)
    @Agent(value = "Argues from a pragmatist perspective", name = "Pragmatist")
    String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);
}
