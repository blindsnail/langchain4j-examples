package tutorials.goal_oriented;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import util.ChatModelProvider;

import java.nio.file.Path;
import java.util.Map;

public class GoalOrientedExample {
    static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        HoroscopeGenerator horoscopeGenerator = AgenticServices.agentBuilder(HoroscopeGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputKey("horoscope")
                .build();

        PersonExtractor personExtractor = AgenticServices.agentBuilder(PersonExtractor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("person")
                .build();

        SignExtractor signExtractor = AgenticServices.agentBuilder(SignExtractor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("sign")
                .build();

        Writer writer = AgenticServices.agentBuilder(Writer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("writeup")
                .build();

        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey("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")
                .build();

        StoryFinder storyFinder = AgenticServices.agentBuilder(StoryFinder.class)
                .chatModel(CHAT_MODEL)
                .tools(new WebSearchTool(webSearchEngine))
                .outputKey("story")
                .build();

        AgentMonitor monitor = new AgentMonitor();

        UntypedAgent horoscopeAgent = AgenticServices.plannerBuilder()
                .subAgents(horoscopeGenerator, personExtractor, signExtractor, writer, storyFinder)
                .listener(monitor)
                .outputKey("writeup")
                .planner(GoalOrientedPlanner::new)
                .build();

        Map<String, Object> input = Map.of("prompt", "My name is Mario and my zodiac sign is pisces");
        String writeup = (String) horoscopeAgent.invoke(input);
        System.out.println(writeup);

        HtmlReportGenerator.generateReport(monitor, Path.of("goal-oriented-example.html"));
    }
}
