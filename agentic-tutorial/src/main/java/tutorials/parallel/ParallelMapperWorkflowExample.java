package tutorials.parallel;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.BatchHoroscopeAgent;
import tutorials.Person;
import tutorials.PersonAstrologyAgent;
import util.ChatModelProvider;

import java.util.List;
import java.util.concurrent.Executors;

public class ParallelMapperWorkflowExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        PersonAstrologyAgent personAstrologyAgent = AgenticServices
                .agentBuilder(PersonAstrologyAgent.class)
                .chatModel(CHAT_MODEL)
                .outputKey("horoscope")
                .build();

        BatchHoroscopeAgent agent = AgenticServices
                .parallelMapperBuilder(BatchHoroscopeAgent.class)
                .subAgents(personAstrologyAgent)
                .executor(Executors.newFixedThreadPool(3))
                .build();

        List<Person> persons = List.of(
                new Person("Mario", "aries"),
                new Person("Luigi", "pisces"),
                new Person("Peach", "leo"));

        List<String> horoscopes = agent.generateHoroscopes(persons);
        System.out.println(horoscopes);
    }
}
