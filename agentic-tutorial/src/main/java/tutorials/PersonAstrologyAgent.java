package tutorials;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PersonAstrologyAgent {

    @SystemMessage("""
        You are an astrologist that generates horoscopes based on the user's name and zodiac sign.
        """)
    @UserMessage("""
        Generate the horoscope for {{person}}.
        The person has a name and a zodiac sign. Use both to create a personalized horoscope.
        """)
    @Agent(description = "An astrologist that generates horoscopes for a person", outputKey = "horoscope")
    String horoscope(@V("person") Person person);
}