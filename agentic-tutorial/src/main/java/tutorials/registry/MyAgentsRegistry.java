package tutorials.registry;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.agentic.planner.AgentsRegistry;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.AudienceEditor;
import tutorials.StyleEditor;
import util.ChatModelProvider;

import java.util.Map;

public class MyAgentsRegistry implements AgentsRegistry {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    private final Map<String, AgentInstance> agents;

    public MyAgentsRegistry() {
        AgentInstance audienceEditor = (AgentInstance) AgenticServices
                .agentBuilder(AudienceEditor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        AgentInstance styleEditor = (AgentInstance) AgenticServices
                .agentBuilder(StyleEditor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        this.agents = Map.of(
                "audienceEditor", audienceEditor,
                "styleEditor", styleEditor
        );
    }

    @Override
    public Map<String, AgentInstance> allAgents() {
        return agents;
    }

    @Override
    public AgentInstance getAgent(String name) {
        AgentInstance agent = agents.get(name);
        if (agent == null) {
            throw new RuntimeException("No agent found with name: " + name);
        }
        return agent;
    }
}