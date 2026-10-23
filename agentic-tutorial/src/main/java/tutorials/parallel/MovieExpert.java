package tutorials.parallel;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface MovieExpert {
    @UserMessage("""
            You are a great evening planner.
            Propose a list of 3 movies matching the given mood.
            The mood is {mood}.
            Provide a list with the 3 items and nothing else.
            """)
    @Agent(outputKey = "movies")
    List<String> findMovies(@V("mood") String mood);

    @ChatModelSupplier
    static ChatModel chatModel() {
        return DeclarativeParallelWorkflowExample.CHAT_MODEL;
    }
}