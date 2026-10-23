import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;

public class ElasticsearchEmbeddingStoreExample {

    public static void main(String[] args) throws IOException {

        try (ElasticsearchContainer elastic =
                new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.5.2")
                        .withPassword("changeme")

        ) {
            elastic.start();

            ElasticsearchClient esClient = ElasticsearchClient.of(builder -> builder
                    .host("http://" + elastic.getHttpHostAddress()) // 直接传入 http 地址
                    .usernameAndPassword("elastic", "changeme")
                    .sslContext(elastic.createSslContextFromCa())
            );

            EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                    .client(esClient)
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            esClient.indices().refresh(r -> r);


            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            EmbeddingSearchResult<TextSegment> relevant = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .build());
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.matches().get(0);

            System.out.println(embeddingMatch.score()); // 0.8138435
            System.out.println(embeddingMatch.embedded().text()); // I like football.

            esClient.close();
        }
    }
}
