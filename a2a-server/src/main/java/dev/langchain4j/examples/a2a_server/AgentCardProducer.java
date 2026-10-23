package dev.langchain4j.examples.a2a_server;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.a2aproject.sdk.server.PublicAgentCard;
import org.a2aproject.sdk.spec.AgentInterface;

import java.util.Collections;
import java.util.List;

import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentSkill;

@ApplicationScoped
public class AgentCardProducer {

    @Produces
    @PublicAgentCard
    public AgentCard agentCard() {
        return AgentCard.builder()
                .name("Creative Writer")
                .description("根据主题生成故事")
                .url("http://localhost:8080/")
                .version("1.0.0")
                .capabilities(AgentCapabilities.builder().streaming(false).build())
                .defaultInputModes(List.of("text/plain"))
                .defaultOutputModes(List.of("text/plain"))
                .supportedInterfaces(List.of(new AgentInterface("JSONRPC", "http://localhost:8080/")))
                .skills(Collections.singletonList(
                        AgentSkill.builder()
                                .id("generate_story")
                                .name("生成故事")
                                .description("根据给定主题生成简短故事")
                                .tags(Collections.singletonList("creative"))
                                .build()
                ))
                .build();
    }
}
