package Tutorials;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.community.data.document.graph.GraphDocument;
import dev.langchain4j.community.data.document.graph.GraphNode;
import dev.langchain4j.community.data.document.graph.GraphEdge;
import dev.langchain4j.community.data.document.transformer.graph.GraphTransformer;
import dev.langchain4j.community.data.document.transformer.graph.LLMGraphTransformer;

import java.time.Duration;
import java.util.Set;

public class GraphTransformerExample {
    public static void main(String[] args) {
        // Create a GraphTransformer backed by an LLM
        GraphTransformer transformer = LLMGraphTransformer.builder()
                .model(OpenAiChatModel.builder()
                        .baseUrl("https://api.deepseek.com/v1") // 关键：指向 DeepSeek 的 API
                        .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                        .modelName("deepseek-flash")
                        .timeout(Duration.ofSeconds(60))
                        .build())
                .build();

        // Input document
        Document document = Document.from("Barack Obama was born in Hawaii and served as the 44th President of the United States.");

        // Transform the document
        GraphDocument graphDocument = transformer.transform(document);

        // Access nodes and relationships
        Set<GraphNode> nodes = graphDocument.nodes();
        Set<GraphEdge> relationships = graphDocument.relationships();

        nodes.forEach(System.out::println);
        relationships.forEach(System.out::println);
    }
}
