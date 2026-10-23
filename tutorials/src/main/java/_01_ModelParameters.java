import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static java.time.Duration.ofSeconds;

public class _01_ModelParameters {

    public static void main(String[] args) {

        // OpenAI parameters are explained here: https://platform.openai.com/docs/api-reference/chat/create

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(ApiKeys.DEEPSEEK_API_KEY)
                .modelName("deepseek-flash")
                .temperature(0.3)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "Explain in three lines how to make a beautiful painting";

        String response = model.chat(prompt);

        System.out.println(response);
    }
}
