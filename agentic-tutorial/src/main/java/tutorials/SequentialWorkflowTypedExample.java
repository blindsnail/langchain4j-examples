package tutorials;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;

import java.io.IOException;

public class SequentialWorkflowTypedExample {

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

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

        NovelCreator novelCreator = AgenticServices
                .sequenceBuilder(NovelCreator.class)
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        String story = novelCreator.createNovel("dragons and wizards", "young adults", "fantasy");
        System.out.println(story);
    }
}