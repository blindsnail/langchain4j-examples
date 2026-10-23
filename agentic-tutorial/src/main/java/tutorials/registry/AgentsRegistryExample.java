package tutorials.registry;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgentsRegistry;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.CreativeWriter;
import util.ChatModelProvider;

import java.util.Map;

public class AgentsRegistryExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        AgentsRegistry registry = AgentsRegistry.get();

        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        AgentInstance audienceEditor = registry.getAgent("audienceEditor");
        AgentInstance styleEditor = registry.getAgent("styleEditor");

        UntypedAgent novelCreator = AgenticServices.sequenceBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        String story = (String) novelCreator.invoke(Map.of(
                "topic", "dragons and wizards",
                "style", "fantasy",
                "audience", "young adults"));

        System.out.println(story);
    }
}
