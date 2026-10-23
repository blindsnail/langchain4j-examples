package tutorials;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.util.Map;

public class OptionalAgentExample {
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(CHAT_MODEL)
                .outputKey("story")
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

        // No "audience" key is provided, so the audienceEditor will be skipped
        Map<String, Object> input = Map.of(
                "topic", "dragons and wizards",
                "style", "fantasy"
        );

        String story = (String) novelCreator.invoke(input);
        System.out.println(story);
    }
}
