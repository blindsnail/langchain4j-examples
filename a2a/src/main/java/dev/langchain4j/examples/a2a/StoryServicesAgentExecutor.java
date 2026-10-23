package dev.langchain4j.examples.a2a;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.TextPart;

import java.util.List;

/**
 * Routes the A2A message to a service declared in the agent card.
 * One text part invokes the creative writer; two text parts invoke the style scorer
 * with {@code story} followed by {@code style}.
 */
public class StoryServicesAgentExecutor implements AgentExecutor {

    private final CreativeWriter creativeWriter;
    private final StyleScorer styleScorer;

    public StoryServicesAgentExecutor(CreativeWriter creativeWriter, StyleScorer styleScorer) {
        this.creativeWriter = creativeWriter;
        this.styleScorer = styleScorer;
    }

    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }
        emitter.startWork();

        List<String> input = textParts(context.getMessage());
        String response = switch (input.size()) {
            case 1 -> creativeWriter.generateStory(input.getFirst());
            case 2 -> Double.toString(styleScorer.scoreStyle(input.getFirst(), input.get(1)));
            default -> throw new IllegalArgumentException(
                    "Send one text part for generate_story, or two text parts for score_style: story then style.");
        };

        emitter.addArtifact(List.of(new TextPart(response, null)));
        emitter.complete();
    }

    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }

    private static List<String> textParts(Message message) {
        if (message == null) {
            return List.of();
        }
        return message.parts().stream()
                .filter(TextPart.class::isInstance)
                .map(TextPart.class::cast)
                .map(TextPart::text)
                .toList();
    }
}
