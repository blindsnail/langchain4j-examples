package tutorials.debate_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EthicsJudge {

    @UserMessage("""
            You are an impartial ethics judge.
            Review the debate context where multiple debaters have argued about a question from different perspectives.
            Synthesize their arguments and provide a balanced, well-reasoned final verdict in 3-4 sentences.
            Debate context: {{debateContext}}
            """)
    @Agent(value = "Renders a final verdict by synthesizing debate arguments", name = "Judge")
    String judge(@V("debateContext") String debateContext);
}
