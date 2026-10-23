package tutorials.streaming;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.V;

public interface StreamingReviewedWriter {

    @Agent
    TokenStream writeStory(@V("topic") String topic, @V("audience") String audience, @V("style") String style);
}