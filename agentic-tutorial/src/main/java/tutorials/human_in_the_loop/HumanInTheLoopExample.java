package tutorials.human_in_the_loop;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.internal.SuspendedResponse;
import dev.langchain4j.agentic.scope.*;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class HumanInTheLoopExample {
    public static void main(String[] args) {
        FileSystemAgenticScopeStore agenticScopeStore = new FileSystemAgenticScopeStore(Path.of("agentic-scope-store"));

        AgenticScopePersister.setStore(agenticScopeStore);

        // Step 1: Validate the order and write results to shared state
        AgenticServices.AgenticScopeAction validateOrder = AgenticServices.agentAction(scope -> {
            String order = scope.readState("order", "");
            scope.writeState("validated_order", "VALIDATED: " + order);
        });

        // Step 2: Pause for human approval using SuspendedResponse
        HumanInTheLoop approvalGate = AgenticServices.humanInTheLoopBuilder()
                .description("Wait for manager approval on large orders")
                .outputKey("approval")
                .responseProvider(scope -> new SuspendedResponse<>("manager-approval"))
                .build();

        // Step 3: Finalize based on the approval decision
        AgenticServices.AgenticScopeAction shipOrder = AgenticServices.agentAction(scope -> {
            String validated = scope.readState("validated_order", "");
            String approval = scope.readState("approval", "");
            scope.writeState("result", "Order " + validated + " — " + approval);
        });

        OrderWorkflow workflow = AgenticServices.sequenceBuilder(OrderWorkflow.class)
                .subAgents(validateOrder, approvalGate, shipOrder)
                .outputKey("result")
                .build();

        final String orderId = "order-12345";

        AgenticScopeKey key = new AgenticScopeKey(OrderWorkflow.class.getName(), orderId);
        Optional<DefaultAgenticScope> optional = agenticScopeStore.load(key);
        if (optional.isPresent() && optional.get().hasState("result")) {
            System.out.println("COMPLETED. result: " + optional.get().readState("result"));
        } else {
            ResultWithAgenticScope<String> result = workflow.processOrder(orderId, "1000 widgets");
            if (result.suspended()) {
                // Workflow suspended — waiting for human input
                AgenticScope scope = result.agenticScope();
                Set<String> pendingIds = scope.pendingResponseIds(); // → ["manager-approval"]
                // Store the scope/pendingIds for your UI / REST API to present to the human
                System.out.println("PENDING. Ids: " + pendingIds);
                pendingIds.forEach(result::completePendingResponse);
            }
        }
    }
}
