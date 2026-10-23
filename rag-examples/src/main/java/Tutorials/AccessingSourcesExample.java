package Tutorials;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;
import static shared.Utils.glob;
import static shared.Utils.toPath;

public class AccessingSourcesExample {
    public static void main(String[] args) {
        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        DefaultContentInjector contentInjector = DefaultContentInjector.builder()
                .metadataKeysToInclude(List.of("url", "file_name", "index", "absolute_directory_path"))
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentInjector(contentInjector)
                .contentRetriever(createContentRetriever())
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel) // it should use OpenAI LLM
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // it should remember 10 latest messages
                .retrievalAugmentor(retrievalAugmentor)
                .build();

        Result<String> result = assistant.chat("Who is John Doe?");
        System.out.println("content: " + result.content());

        System.out.println("used " + result.sources().size() + " sources");
        result.sources().forEach(System.out::println);
    }

    private static ContentRetriever createContentRetriever() {
        List<Document> documents = loadDocuments(toPath("documents/"), glob("*.txt"));

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);

        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }
}
