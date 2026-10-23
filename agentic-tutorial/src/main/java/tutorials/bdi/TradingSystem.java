package tutorials.bdi;

import dev.langchain4j.service.V;

public interface TradingSystem {
    String trade(@V("marketData") String marketData, @V("portfolio") String portfolio);
}