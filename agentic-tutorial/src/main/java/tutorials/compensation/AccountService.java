package tutorials.compensation;

import dev.langchain4j.agent.tool.CompensateFor;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class AccountService {

    @Tool("Credits the given amount to the account")
    String credit(@P(name = "amount") int amount) {
        // perform the credit
        return "credited " + amount;
    }

    @CompensateFor("credit")
    void reverseCredit(int amount) {
        // reverse the credit
    }
}