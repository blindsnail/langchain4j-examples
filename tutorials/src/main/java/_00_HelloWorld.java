import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class _00_HelloWorld {

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.DEEPSEEK_API_KEY)
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com/v1")
                .build();

        String answer = model.chat("Say Hello World");

        System.out.println(answer);
    }
}
