package tutorials.p2p;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

public class ArxivCrawler {

    private static final URI ARXIV_API = URI.create("https://export.arxiv.org/api/query");
    private static final int MAX_RESULTS = 5;

    private final HttpClient httpClient;
    private final URI apiEndpoint;

    public ArxivCrawler() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), ARXIV_API);
    }

    ArxivCrawler(HttpClient httpClient, URI apiEndpoint) {
        this.httpClient = httpClient;
        this.apiEndpoint = apiEndpoint;
    }

    @Tool("Search arXiv for recent and relevant scientific papers on a topic, returning titles, authors, abstracts, and links")
    public String searchPapers(@P(name = "topic") String topic) {
        if (topic == null || topic.isBlank()) {
            return "Please provide a topic to search for.";
        }

        String encodedTopic = URLEncoder.encode(topic.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(apiEndpoint + "?search_query=all:" + encodedTopic
                + "&start=0&max_results=" + MAX_RESULTS + "&sortBy=relevance");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/atom+xml")
                .header("User-Agent", "LangChain4j-ArxivCrawler/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return "arXiv search failed with HTTP status " + response.statusCode() + ".";
            }
            return formatResults(parseEntries(response.body()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "arXiv search was interrupted.";
        } catch (Exception e) {
            return "Unable to search arXiv: " + e.getMessage();
        }
    }

    private List<Paper> parseEntries(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        NodeList entries = document.getElementsByTagNameNS("*", "entry");
        List<Paper> papers = new ArrayList<>();
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String title = childText(entry, "title");
            String summary = childText(entry, "summary");
            String published = childText(entry, "published");
            String id = childText(entry, "id");

            NodeList authors = entry.getElementsByTagNameNS("*", "author");
            List<String> names = new ArrayList<>();
            for (int j = 0; j < authors.getLength(); j++) {
                String name = childText((Element) authors.item(j), "name");
                if (!name.isBlank()) {
                    names.add(name);
                }
            }
            papers.add(new Paper(title, String.join(", ", names), published, id, summary));
        }
        return papers;
    }

    private String childText(Element parent, String name) {
        NodeList children = parent.getElementsByTagNameNS("*", name);
        if (children.getLength() == 0) {
            return "";
        }
        Node node = children.item(0);
        return node.getTextContent().replaceAll("\\s+", " ").trim();
    }

    private String formatResults(List<Paper> papers) {
        if (papers.isEmpty()) {
            return "No arXiv papers were found for this topic.";
        }
        StringBuilder result = new StringBuilder("Found ").append(papers.size()).append(" arXiv paper(s):");
        for (int i = 0; i < papers.size(); i++) {
            Paper paper = papers.get(i);
            result.append("\n\n").append(i + 1).append(". ").append(paper.title());
            if (!paper.authors().isBlank()) result.append("\nAuthors: ").append(paper.authors());
            if (!paper.published().isBlank()) result.append("\nPublished: ").append(paper.published());
            result.append("\nAbstract: ").append(paper.summary());
            result.append("\nLink: ").append(paper.link());
        }
        return result.toString();
    }

    private record Paper(String title, String authors, String published, String link, String summary) {
    }
}
