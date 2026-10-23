package dev.langchain4j.examples.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a conversation into the prompt that DeepSeek's chat models actually receive.
 *
 * <p>The prompt is rendered with the {@code chat_template} that DeepSeek ships in its official
 * {@code tokenizer_config.json}, executed by jinjava, a Jinja2 engine for Java. The template is therefore never
 * duplicated in this code base: a DeepSeek release that ships a different template is picked up automatically, down
 * to its quirks. Those quirks matter, because the API server applies the very same template before it tokenizes the
 * prompt.</p>
 *
 * <p>The template has no notion of a message name, so {@link UserMessage#name()} is ignored, and a user message with
 * several text parts is rendered as those parts joined by a line feed.</p>
 */
public final class DeepSeekChatTemplate {

    private static final String CONFIG_RESOURCE = "/deepseek-v4/tokenizer_config.json";

    private final Jinjava jinjava;
    private final String chatTemplate;
    private final String bosToken;
    private final String eosToken;

    private DeepSeekChatTemplate(Jinjava jinjava, String chatTemplate, String bosToken, String eosToken) {
        this.jinjava = jinjava;
        this.chatTemplate = chatTemplate;
        this.bosToken = bosToken;
        this.eosToken = eosToken;
    }

    /**
     * Loads the chat template from DeepSeek's official {@code tokenizer_config.json}.
     *
     * @return the chat template
     */
    public static DeepSeekChatTemplate load() {
        JsonNode config = readConfig();
        Jinjava jinjava = new Jinjava(JinjavaConfig.newBuilder()
                .withTrimBlocks(true)
                .withLstripBlocks(true)
                .build());
        return new DeepSeekChatTemplate(
                jinjava,
                config.path("chat_template").asText(),
                config.path("bos_token").path("content").asText(),
                config.path("eos_token").path("content").asText());
    }

    /**
     * Renders the given messages into the prompt string of a DeepSeek chat model.
     *
     * @param messages the messages of the conversation
     * @return the rendered prompt
     * @throws IllegalArgumentException when a message type or a content type is not supported by DeepSeek's template
     */
    public String render(Iterable<ChatMessage> messages) {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("messages", toTemplateMessages(messages));
        bindings.put("bos_token", bosToken);
        bindings.put("eos_token", eosToken);
        bindings.put("add_generation_prompt", false);
        return jinjava.render(chatTemplate, bindings);
    }

    /**
     * Converts the messages into the structure that the chat template expects: a list of maps with a {@code role} and
     * a {@code content}, plus {@code tool_calls} for an assistant message that requests tool executions.
     */
    private static List<Map<String, Object>> toTemplateMessages(Iterable<ChatMessage> messages) {
        List<Map<String, Object>> templateMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            Map<String, Object> templateMessage = new LinkedHashMap<>();
            if (message instanceof SystemMessage systemMessage) {
                templateMessage.put("role", "system");
                templateMessage.put("content", systemMessage.text());
            } else if (message instanceof UserMessage userMessage) {
                templateMessage.put("role", "user");
                templateMessage.put("content", textOf(userMessage));
            } else if (message instanceof AiMessage aiMessage) {
                templateMessage.put("role", "assistant");
                templateMessage.put("content", aiMessage.text());
                if (aiMessage.hasToolExecutionRequests()) {
                    templateMessage.put("tool_calls", toToolCalls(aiMessage.toolExecutionRequests()));
                }
            } else if (message instanceof ToolExecutionResultMessage toolResultMessage) {
                templateMessage.put("role", "tool");
                templateMessage.put("content", toolResultMessage.text());
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
            }
            templateMessages.add(templateMessage);
        }
        return templateMessages;
    }

    private static List<Map<String, Object>> toToolCalls(List<ToolExecutionRequest> requests) {
        List<Map<String, Object>> toolCalls = new ArrayList<>();
        for (ToolExecutionRequest request : requests) {
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", request.name());
            function.put("arguments", request.arguments());

            Map<String, Object> toolCall = new LinkedHashMap<>();
            toolCall.put("type", "function");
            toolCall.put("function", function);
            toolCalls.add(toolCall);
        }
        return toolCalls;
    }

    private static String textOf(UserMessage message) {
        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (Content content : message.contents()) {
            if (!(content instanceof TextContent textContent)) {
                throw new IllegalArgumentException(
                        "DeepSeek's chat template does not support content type: " + content.getClass().getName());
            }
            if (!first) {
                text.append('\n');
            }
            first = false;
            text.append(textContent.text());
        }
        return text.toString();
    }

    private static JsonNode readConfig() {
        try (InputStream config = DeepSeekChatTemplate.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (config == null) {
                throw new IllegalStateException(
                        "DeepSeek tokenizer config not found on the classpath: " + CONFIG_RESOURCE);
            }
            return new ObjectMapper().readTree(config);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the DeepSeek tokenizer config: " + CONFIG_RESOURCE, e);
        }
    }
}
