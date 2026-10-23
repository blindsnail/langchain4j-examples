package tutorials;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.service.V;

import java.util.List;

public interface BatchHoroscopeAgent extends AgentInstance {

    @Agent
    List<String> generateHoroscopes(@V("persons") List<Person> persons);
}