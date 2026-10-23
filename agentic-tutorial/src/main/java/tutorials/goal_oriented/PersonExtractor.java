package tutorials.goal_oriented;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import tutorials.Person;

public interface PersonExtractor {

    @UserMessage("Extract a person from the following prompt: {{prompt}}")
    @Agent("Extract a person from user's prompt")
    Person extractPerson(@V("prompt") String prompt);
}
