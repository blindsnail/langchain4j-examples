import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class SimpleServiceExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        Assistant assistant = AiServices.create(Assistant.class, chatModel);

        String answer = assistant.chat("What is the square root of 475695037565?");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
