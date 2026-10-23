package dev.langchain4j.examples.a2a_server;

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
        // New requests need a task record before subsequent status/artifact events.
        if (context.getTask() == null) {
            emitter.submit();
        }

        // 1. 标记任务开始
        emitter.startWork();

        // 2. 从 A2A 请求中提取用户输入
        String topic = context.getUserInput("");

        // 3. 调用你的 LangChain4j Agent
        String story = creativeWriter.generateStory(topic);

        // 4. 将结果作为 Artifact 发送回去
        emitter.addArtifact(List.of(new TextPart(story, null)));

        // 5. 标记任务完成
        emitter.complete();
    }

    @Override
    public void cancel(RequestContext context, AgentEmitter emitter) {
        emitter.cancel();
    }
}
