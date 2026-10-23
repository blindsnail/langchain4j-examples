package dev.langchain4j.example;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class JevDecisionModelExample {
    public static void main(String[] args) {
        DecisionModel decisionModel = TypeSafeDecisionModel.builder()
                .apiKey("apikey_2127599576886d544ac8a132ba1593afbf84_e68362f0723b68ca92046121f7c419736361e063a2b3338aaad784da660adb99")
                .modelName("jev-1.13.0")
                .logRequests(true)
                .logResponses(true)
                .build();

        final String input = "Help! My payouts have been failing for 3 days and nobody answers my emails.";

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

        YesNoQuestion refundRequested = YesNoQuestion.builder()
                .text("Does the customer ask for a refund?")
                .yesWhen("The customer explicitly asks for their money back")
                .noWhen("The customer only asks about a charge")
                .build();

        ChoiceQuestion sentiment = ChoiceQuestion.builder()
                .text("What is the sentiment?")
                .option("positive")
                .option("negative")
                .option("neutral")
                .build();

        ScaleQuestion severity = ScaleQuestion.builder()
                .text("How severe is the incident?")
                .level("Minor", "No customer is affected")
                .level("Major", "Some customers are affected")
                .level("Critical", "No customer can use the product")
                .build();

        DecisionRequest request = DecisionRequest.builder()
                .input(input)
                .question("team", choiceQuestion)
                .question("urgent", yesNoQuestion)
                .question("frustration", scaleQuestion)
                .question("refund", refundRequested)
                .question("sentiment", sentiment)
                .question("severity", severity)
                .build();

        DecisionResponse response = decisionModel.decide(request);

        System.out.println(response.modelName());
        System.out.println(response.tokenUsage());
        System.out.println(response.choice("team"));
        System.out.println(response.yesNo("urgent"));
        System.out.println(response.scale("frustration"));
        System.out.println(response.yesNo("refund"));
        System.out.println(response.choice("sentiment"));
        System.out.println(response.scale("severity"));
    }
}