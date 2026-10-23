package Tutorials;

import _2_naive.Naive_RAG_Example;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.elasticsearch.ElasticsearchContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import shared.Assistant;

import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;
import static shared.Utils.*;

public class ElasticSearchExample {

    private static final ChatModel CHAT_MODEL = OpenAiChatModel.builder()
            .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
            .apiKey(System.getenv("DEEPSEEK_API_KEY"))
            .modelName("deepseek-flash")
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * This example demonstrates how to implement an "Easy RAG" (Retrieval-Augmented Generation) application.
     * By "easy" we mean that we won't dive into all the details about parsing, splitting, embedding, etc.
     * All the "magic" is hidden inside the "langchain4j-easy-rag" module.
     * <p>
     * If you want to learn how to do RAG without the "magic" of an "Easy RAG", see {@link Naive_RAG_Example}.
     */

    public static void main(String[] args) {

        // First, let's load documents that we want to use for RAG
        List<Document> documents = loadDocuments(toPath("documents/"), glob("*.txt"));

        // Second, let's create an assistant that will have access to our documents
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL) // it should use OpenAI LLM
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // it should remember 10 latest messages
                .contentRetriever(createContentRetriever(documents)) // it should have access to our documents
                .build();

        // Lastly, let's start the conversation with the assistant. We can ask questions like:
        // - Can I cancel my reservation?
        // - I had an accident, should I pay extra?
        startConversationWith(assistant);
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {
        ElasticsearchClient client = ElasticsearchClient.of(ec -> ec
                .host("http://localhost:9200")
                .usernameAndPassword("elastic", "changeme")
        );

        // Here, we create an empty in-memory store for our documents and their embeddings.
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .client(client)
                .indexName("rag_demo")
                .build();

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);

        // Lastly, let's create a content retriever from an embedding store.
        return ElasticsearchContentRetriever.builder()
                .client(client)
                .indexName("rag_demo")
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.7)
                .build();
    }
}
