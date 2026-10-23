import static dev.langchain4j.internal.Utils.getOrDefault;

public class ApiKeys {

    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");

    public static final String DEEPSEEK_API_KEY = System.getenv("DEEPSEEK_API_KEY");

    public static final String RAPID_API_KEY = System.getenv("RAPID_API_KEY");
}
