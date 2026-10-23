package tutorials.conditional.declarative;

import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.V;

public interface ExpertsAgent {

    @ConditionalAgent(
            outputKey = "response",
            subAgents = {MedicalExpert.class, TechnicalExpert.class, LegalExpert.class}
    )
    String askExpert(@V("request") String request);

    @ActivationCondition(MedicalExpert.class)
    static boolean activateMedical(@V("category") RequestCategory category) {
        return category == RequestCategory.MEDICAL;
    }

    @ActivationCondition(TechnicalExpert.class)
    static boolean activateTechnical(@V("category") RequestCategory category) {
        return category == RequestCategory.TECHNICAL;
    }

    @ActivationCondition(LegalExpert.class)
    static boolean activateLegal(@V("category") RequestCategory category) {
        return category == RequestCategory.LEGAL;
    }

    @ChatModelSupplier
    static ChatModel chatModel() {
        return ConditionalWorkflowExample.CHAT_MODEL;
    }
}