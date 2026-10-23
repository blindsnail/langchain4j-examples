import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import org.junit.jupiter.api.Test;

public class DecisionModelTest {
    private final static DecisionModel DECISION_MODEL = TypeSafeDecisionModel.builder()
            .apiKey("apikey_2127599576886d544ac8a132ba1593afbf84_e68362f0723b68ca92046121f7c419736361e063a2b3338aaad784da660adb99")
            .modelName("jev-1.13.0")
            .build();

    private final static String INPUT = "Help! My payouts have been failing for 3 days and nobody answers my emails.";

    @Test
    void test1() {

        ChoiceQuestion choiceQuestion = ChoiceQuestion.builder()
                .text("Which team should handle this ticket?")
                .option("billing", "Payments, payouts, invoices, refunds")
                .option("support", "Problems using the product")
                .option("sales", "Pricing, upgrades, new accounts")
                .build();

        YesNoQuestion yesNoQuestion = YesNoQuestion.of("Does this need attention today?");

        ScaleQuestion scaleQuestion = ScaleQuestion.builder()
                .text("How frustrated is the customer?")
                .level("Calm")
                .level("Frustrated")
                .level("Angry")
                .build();

        DecisionRequest request = DecisionRequest.builder()
                .input(INPUT)
                .question("team", choiceQuestion)
                .question("urgent", yesNoQuestion)
                .question("frustration", scaleQuestion)
                .build();

        DecisionResponse response = DECISION_MODEL.decide(request);

        System.out.println(response.modelName());
        System.out.println(response.tokenUsage());
        System.out.println(response.choice("team"));
        System.out.println(response.yesNo("urgent"));
        System.out.println(response.scale("frustration"));
    }

}
