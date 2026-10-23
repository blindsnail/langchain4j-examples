package tutorials.debate_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

public interface DeontologicalDebater {

    @UserMessage("""
            You are a deontological ethics debater.
            Consider the following question and argue based on moral rules, duties, and rights.
            If previous debate context is provided, consider the other debaters' arguments and refine your position.
            Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
            Question: {{question}}
            Previous debate context: {{debateContext}}
            """)
    @Agent(value = "Argues from a deontological ethics perspective", name = "Deontologist")
    String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);
}
