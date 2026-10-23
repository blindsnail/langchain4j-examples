import dev.langchain4j.examples.shared.DeepSeekTokenCountEstimator;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.concurrent.CompletableFuture;

public class _04_Streaming {

    public static void main(String[] args) {

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(ApiKeys.DEEPSEEK_API_KEY)
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "Write a short funny poem about developers and null-pointers, 10 lines maximum";

        System.out.println("Nr of chars: " + prompt.length());
        System.out.println("Estimated nr of tokens: "
                + new DeepSeekTokenCountEstimator().estimateTokenCountInText(prompt));

        CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

        model.chat(prompt, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("\n\nDone streaming");
                futureChatResponse.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                futureChatResponse.completeExceptionally(error);
            }
        });

        futureChatResponse.join();
    }
}
