package dev.langchain4j.examples.a2a;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CreativeWriter {

    @UserMessage("""
            You are a creative writer. Write a story of no more than three sentences about the topic below.
            Return only the story.
            Topic: {{topic}}
            """)
    @Agent("Generates a short story based on a topic")
    String generateStory(@V("topic") String topic);
}
