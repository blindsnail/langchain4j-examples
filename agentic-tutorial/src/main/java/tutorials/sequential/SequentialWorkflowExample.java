package tutorials.sequential;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.*;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.AudienceEditor;
import tutorials.CreativeWriter;
import tutorials.StyleEditor;
import util.ChatModelProvider;

import java.io.IOException;
import java.util.Map;

public class SequentialWorkflowExample {

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .listener(new AgentListener() {
                    @Override
                    public void beforeAgentInvocation(AgentRequest request) {
                        System.out.println("Invoking CreativeWriter with topic: " + request.inputs().get("topic"));
                    }

                    @Override
                    public void afterAgentInvocation(AgentResponse response) {
                        System.out.println("CreativeWriter generated this story: " + response.output());
                    }
                })
                .build();

        AudienceEditor audienceEditor = AgenticServices
                .agentBuilder(AudienceEditor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
                .build();

        UntypedAgent novelCreator = AgenticServices
                .sequenceBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        Map<String, Object> input = Map.of(
                "topic", "dragons and wizards",
                "style", "fantasy",
                "audience", "young adults"
        );

        String story = (String) novelCreator.invoke(input);
        System.out.println(story);
    }
}