package tutorials.debate_pattern;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface EthicsPanel {

    @Agent
    String debate(@V("question") String question);
}