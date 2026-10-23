package dev.langchain4j.example;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import dev.langchain4j.service.decision.Choice;
import dev.langchain4j.service.decision.Decide;
import dev.langchain4j.service.decision.DecisionServices;

public class JevDecisionServiceExample {
    public static void main(String[] args) {
        DecisionModel decisionModel = TypeSafeDecisionModel.builder()
                .apiKey("apikey_2127599576886d544ac8a132ba1593afbf84_e68362f0723b68ca92046121f7c419736361e063a2b3338aaad784da660adb99")
                .modelName("jev-1.13.0")
                .logRequests(true)
                .logResponses(true)
                .build();

        SupportDesk supportDesk = DecisionServices.create(SupportDesk.class, decisionModel);

        YesNoAnswer spam = supportDesk.isSpam("Congratulations! You won a free cruise!");   // true
        System.out.println("is it spam? " + spam);
        Choice<Team> team = supportDesk.route("I was charged twice this month");                // BILLING
        System.out.println("team: " + team);
    }
}

enum Team {
    @Description("Payments, payouts, invoices, refunds") BILLING,
    @Description("Problems using the product") SUPPORT,
    @Description("Pricing, upgrades, new accounts") SALES
}

interface SupportDesk {

    @Decide("Is this message spam?")
    YesNoAnswer isSpam(String message);

    @Decide("Which team should handle this ticket?")
    Choice<Team> route(String ticket);
}