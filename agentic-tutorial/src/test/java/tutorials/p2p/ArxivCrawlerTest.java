package tutorials.p2p;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArxivCrawlerTest {

    private HttpServer server;
    private AtomicReference<Integer> status;
    private AtomicReference<String> responseBody;
    private AtomicReference<String> requestUri;
    private ArxivCrawler crawler;

    @BeforeEach
    void startServer() throws IOException {
        status = new AtomicReference<>(200);
        responseBody = new AtomicReference<>(atomFeedWithPaper());
        requestUri = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/query", exchange -> {
            requestUri.set(exchange.getRequestURI().toString());
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/atom+xml; charset=utf-8");
            exchange.sendResponseHeaders(status.get(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/query");
        crawler = new ArxivCrawler(HttpClient.newHttpClient(), endpoint);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void returnsPaperMetadataAndBuildsEncodedSearchRequest() {
        String result = crawler.searchPapers("black holes & gravity");

        assertTrue(result.contains("Found 1 arXiv paper(s)"));
        assertTrue(result.contains("A Study of Black Holes"));
        assertTrue(result.contains("Ada Lovelace, Alan Turing"));
        assertTrue(result.contains("Published: 2025-01-02T03:04:05Z"));
        assertTrue(result.contains("Black holes provide a useful test case."));
        assertTrue(result.contains("https://arxiv.org/abs/2501.01234"));
        assertTrue(requestUri.get().contains("search_query=all:black+holes+%26+gravity"));
        assertTrue(requestUri.get().contains("max_results=5"));
    }

    @Test
    void reportsWhenNoPapersAreFound() {
        responseBody.set(emptyAtomFeed());

        assertEquals("No arXiv papers were found for this topic.", crawler.searchPapers("unknown topic"));
    }

    @Test
    void reportsHttpErrors() {
        status.set(503);

        assertEquals("arXiv search failed with HTTP status 503.", crawler.searchPapers("black holes"));
    }

    @Test
    void doesNotMakeRequestForBlankTopic() {
        assertEquals("Please provide a topic to search for.", crawler.searchPapers("  "));
        assertNull(requestUri.get());
    }

    private static String atomFeedWithPaper() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry>
                    <id>https://arxiv.org/abs/2501.01234</id>
                    <published>2025-01-02T03:04:05Z</published>
                    <title> A Study of Black Holes </title>
                    <summary>Black holes provide a useful test case.</summary>
                    <author><name>Ada Lovelace</name></author>
                    <author><name>Alan Turing</name></author>
                  </entry>
                </feed>
                """;
    }

    private static String emptyAtomFeed() {
        return "<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>Search Results</title></feed>";
    }
}
