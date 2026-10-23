package tutorials.pure_agentic;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BankTool {

    private final Map<String, BigDecimal> accounts = new HashMap<>();

    void createAccount(String user, BigDecimal initialBalance) {
        if (accounts.containsKey(user)) {
            throw new RuntimeException("Account for user " + user + " already exists");
        }
        accounts.put(user, initialBalance);
    }

    BigDecimal getBalance(String user) {
        BigDecimal balance = accounts.get(user);
        if (balance == null) {
            throw new RuntimeException("No balance found for user " + user);
        }
        return balance;
    }

    @Tool("Credit the given user with the given amount and return the new balance")
    BigDecimal credit(@P(name = "userName", description = "The user's name") String user,
                      @P(name = "amount", description = "The amount") BigDecimal amount) {
        BigDecimal balance = accounts.get(user);
        if (balance == null) {
            throw new RuntimeException("No balance found for user " + user);
        }
        BigDecimal newBalance = balance.add(amount);
        accounts.put(user, newBalance);
        return newBalance;
    }

    @Tool("Withdraw the given amount with the given user and return the new balance")
    BigDecimal withdraw(@P(name = "userName", description = "The user's name") String user,
                        @P(name = "amount", description = "The amount") BigDecimal amount) {
        BigDecimal balance = accounts.get(user);
        if (balance == null) {
            throw new RuntimeException("No balance found for user " + user);
        }
        BigDecimal newBalance = balance.subtract(amount);
        accounts.put(user, newBalance);
        return newBalance;
    }
}