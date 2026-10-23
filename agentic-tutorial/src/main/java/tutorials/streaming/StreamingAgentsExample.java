package tutorials.streaming;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.TokenStream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StreamingAgentsExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
//                .logRequests(true)
//                .logResponses(true)
                .build();

        StreamingCreativeWriter creativeWriter = AgenticServices.agentBuilder(StreamingCreativeWriter.class)
                .streamingChatModel(model)
                .outputKey("story")
                .build();

        StreamingAudienceEditor audienceEditor = AgenticServices.agentBuilder(StreamingAudienceEditor.class)
                .streamingChatModel(model)
                .outputKey("story")
                .build();

        StreamingStyleEditor styleEditor = AgenticServices.agentBuilder(StreamingStyleEditor.class)
                .streamingChatModel(model)
                .outputKey("story")
                .build();

        StreamingReviewedWriter novelCreator = AgenticServices.sequenceBuilder(StreamingReviewedWriter.class)
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        TokenStream tokenStream = novelCreator.writeStory("dragons and wizards", "young adults", "fantasy");

        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        tokenStream.onPartialResponse(System.out::print)
                .onCompleteResponse(future::complete)
                .onError(future::completeExceptionally)
                .start();

        future.join();
    }
}
