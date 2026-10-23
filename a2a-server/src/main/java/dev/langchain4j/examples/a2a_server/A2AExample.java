package dev.langchain4j.examples.a2a_server;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;

import java.util.Map;

public class A2AExample {

    public static void main(String[] args) {
        UntypedAgent creativeWriter = AgenticServices
                .a2aBuilder("http://127.0.0.1:8080")
                .inputKeys("topic")
                .outputKey("story")
                .build();

        String result = (String) creativeWriter.invoke(Map.of("topic", "The Matrix"));
        System.out.println(result);
    }
}
