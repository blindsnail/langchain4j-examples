package dev.langchain4j.examples.a2a;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;

import java.util.List;

public class CreativeWriterAgentExecutor implements AgentExecutor {

    private final CreativeWriter creativeWriter;

    public CreativeWriterAgentExecutor(CreativeWriter creativeWriter) {
        this.creativeWriter = creativeWriter;
    }

    @Override
    public void execute(RequestContext context, AgentEmitter emitter) {
        if (context.getTask() == null) {
            emitter.submit();
        }
        emitter.startWork();
        String topic = context.getUserInput("");
        String story = creativeWriter.generateStory(topic);
        emitter.addArtifact(List.of(new TextPart(story, null)));
        emitter.complete();
    }

    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }
}
