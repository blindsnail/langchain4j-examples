package dev.langchain4j.examples.a2a;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.a2aproject.sdk.server.PublicAgentCard;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;

import java.util.List;

@ApplicationScoped
public class A2aAgentCardProducer {

    @Produces
    @PublicAgentCard
    AgentCard publicAgentCard() {
        return AgentCard.builder()
                .name("Creative Writer")
                .description("Generates a short story for a supplied topic")
                .url("http://localhost:8080/")
                .version("1.0.0")
                .supportedInterfaces(List.of(new AgentInterface(
                        "JSONRPC", "http://localhost:8080/")))
                .capabilities(AgentCapabilities.builder().streaming(false).build())
                .defaultInputModes(List.of("text/plain"))
                .defaultOutputModes(List.of("text/plain"))
                .skills(List.of(
                        AgentSkill.builder()
                                .id("generate_story")
                                .name("Generate a story")
                                .description("Writes a short story about a supplied topic")
                                .tags(List.of("creative-writing"))
                                .examples(List.of("A robot discovers a garden on Mars"))
                                .build(),
                        AgentSkill.builder()
                                .id("score_style")
                                .name("Score story style")
                                .description("Scores how well a story matches a requested style")
                                .tags(List.of("review", "style"))
                                .examples(List.of("Story: Once upon a time...; Style: fairy tale"))
                                .build()))
                .build();
    }
}
