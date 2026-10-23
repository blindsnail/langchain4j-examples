package tutorials.pure_agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public class ExchangeAgent {
    @Agent(name = "Exchange Agent", description = """
            A money exchanger that converts a given amount of money from the original to the target currency
            """)
    Double exchange(@V("originalCurrency") String originalCurrency,
                    @V("amount") Double amount,
                    @V("targetCurrency") String targetCurrency) {
        System.out.println("Exchanging from " + originalCurrency + " to " + targetCurrency);
        if (originalCurrency.equals("EUR") && targetCurrency.equals("USD")) {
            return amount * 1.12;
        }
        throw new UnsupportedOperationException();
    }
}
