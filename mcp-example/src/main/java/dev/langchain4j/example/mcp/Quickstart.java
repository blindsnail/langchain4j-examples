package dev.langchain4j.example.mcp;

import com.alibaba.opensandbox.sandbox.Sandbox;
import com.alibaba.opensandbox.sandbox.config.ConnectionConfig;
import com.alibaba.opensandbox.sandbox.domain.exceptions.SandboxException;
import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.Execution;

public class Quickstart {
    public static void main(String[] args) {
        // 1. Configure connection
        ConnectionConfig config = ConnectionConfig.builder()
            .domain("localhost:8080")
            .apiKey("A2C6FE3F-2B4F-4037-9704-D20749FC6D5F")
            .build();

        // 2. Create a Sandbox using try-with-resources
        try (Sandbox sandbox = Sandbox.builder()
                .connectionConfig(config)
                .image("ubuntu")
                .build()) {

            try {
                Execution execution = sandbox.commands().run("echo 'Hello Sandbox!'");
                System.out.println(execution.getLogs().getStdout().get(0).getText());
            } finally {
                sandbox.kill();
            } // try-with-resources closes the client even if kill() fails.

        } catch (SandboxException e) {
            // Handle Sandbox specific exceptions
            System.err.println("Sandbox Error: [" + e.getError().getCode() + "] " + e.getError().getMessage());
            System.err.println("Request ID: " + e.getRequestId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}