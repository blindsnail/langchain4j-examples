package dev.langchain4j.examples.shared;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.TokenCountEstimator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Estimates token usage for DeepSeek models.
 *
 * <p>The counts are produced by DeepSeek's official {@code tokenizer.json}, executed by the HuggingFaceTokenizer of
 * the Deep Java Library (DJL). The tokenizer is a resource of this module, so no network access is needed and the
 * counts match what the DeepSeek API bills for.</p>
 *
 * <p>Conversations are rendered with {@link DeepSeekChatTemplate} before they are tokenized, because DeepSeek's chat
 * template wraps message content into dedicated marker tokens. Those markers are part of the vocabulary, so they are
 * counted as one token each.</p>
 *
 * <p>The estimator supports the text-only message types used by these tutorials. It fails fast for multimodal content,
 * whose token usage cannot be inferred from text length.</p>
 */
public final class DeepSeekTokenCountEstimator implements TokenCountEstimator {

    private static final String TOKENIZER_RESOURCE = "/deepseek-v4/tokenizer.json";

    /**
     * Truncation and padding are switched off on purpose: this class counts tokens, it never truncates a prompt.
     */
    private static final Map<String, String> TOKENIZER_OPTIONS = Map.of(
            "addSpecialTokens", "false",
            "truncation", "false",
            "padding", "false");

    private static final DeepSeekChatTemplate CHAT_TEMPLATE = DeepSeekChatTemplate.load();

    private static final HuggingFaceTokenizer TOKENIZER = loadTokenizer();

    @Override
    public int estimateTokenCountInText(String text) {
        Objects.requireNonNull(text, "text");
        return countTokens(text);
    }

    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        Objects.requireNonNull(message, "message");
        return countTokens(CHAT_TEMPLATE.render(List.of(message)));
    }

    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
        Objects.requireNonNull(messages, "messages");
        return countTokens(CHAT_TEMPLATE.render(messages));
    }

    private static int countTokens(String text) {
        return TOKENIZER.encode(text, false, false).getIds().length;
    }

    private static HuggingFaceTokenizer loadTokenizer() {
        try (InputStream tokenizer = DeepSeekTokenCountEstimator.class.getResourceAsStream(TOKENIZER_RESOURCE)) {
            if (tokenizer == null) {
                throw new IllegalStateException("DeepSeek tokenizer not found on the classpath: " + TOKENIZER_RESOURCE);
            }
            return HuggingFaceTokenizer.newInstance(tokenizer, TOKENIZER_OPTIONS);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the DeepSeek tokenizer: " + TOKENIZER_RESOURCE, e);
        }
    }
}
