package tutorials.sequential;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.model.chat.ChatModel;
import tutorials.AudienceEditor;
import tutorials.CreativeWriter;
import tutorials.StyleEditor;
import util.ChatModelProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SequentialPlannerExample {

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

        AgentMonitor monitor = new AgentMonitor();

        UntypedAgent novelCreator = AgenticServices
                .plannerBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .listener(monitor)
                .outputKey("story")
                .planner(SequentialPlanner::new)
                .build();

        Map<String, Object> input = Map.of(
                "topic", "dragons and wizards",
                "style", "fantasy",
                "audience", "young adults"
        );

        String story = (String) novelCreator.invoke(input);
        System.out.println(story);

        HtmlReportGenerator.generateReport(monitor, Path.of("sequential-planner.html"));
    }
}