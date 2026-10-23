package tutorials.human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface OrderWorkflow extends AgenticScopeAccess {
    @Agent
    ResultWithAgenticScope<String> processOrder(@MemoryId String orderId, @V("order") String orderDetails);
}