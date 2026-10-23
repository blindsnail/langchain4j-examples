package dev.langchain4j.examples.shared;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DeepSeekTokenCountEstimatorTest {

    private final DeepSeekTokenCountEstimator estimator = new DeepSeekTokenCountEstimator();

    @Test
    public void shouldCountTokensOfEnglishChineseAndMixedText() {
        assertEquals(0, estimator.estimateTokenCountInText(""));
        assertEquals(1, estimator.estimateTokenCountInText("hello"));
        assertEquals(1, estimator.estimateTokenCountInText("你好"));
        assertEquals(2, estimator.estimateTokenCountInText("hello你好"));
    }

    @Test
    public void shouldCountRareCharactersAsByteLevelTokens() {
        // A character that is not part of the vocabulary falls back to its four UTF-8 bytes.
        assertEquals(4, estimator.estimateTokenCountInText("𠮷"));
    }

    @Test
    public void shouldCountDeepSeekMarkersAsSingleTokens() {
        assertEquals(1, estimator.estimateTokenCountInText("<｜begin▁of▁sentence｜>"));
        assertEquals(1, estimator.estimateTokenCountInText("<｜end▁of▁sentence｜>"));
        assertEquals(1, estimator.estimateTokenCountInText("<｜User｜>"));
        assertEquals(1, estimator.estimateTokenCountInText("<｜Assistant｜>"));
    }

    @Test
    public void shouldNotTruncateLongText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            text.append("hello world ");
        }

        assertEquals(2001, estimator.estimateTokenCountInText(text.toString()));
    }

    @Test
    public void shouldEstimateSingleMessages() {
        assertEquals(2, estimator.estimateTokenCountInMessage(SystemMessage.from("system")));
        assertEquals(3, estimator.estimateTokenCountInMessage(UserMessage.from("hello")));
        assertEquals(4, estimator.estimateTokenCountInMessage(AiMessage.from("你好")));
    }

    @Test
    public void shouldEstimateConversationWithChatTemplate() {
        List<ChatMessage> messages = List.of(
                SystemMessage.from("system"),
                UserMessage.from("hello"),
                AiMessage.from("你好"));

        assertEquals(7, estimator.estimateTokenCountInMessages(messages));
    }

    @Test
    public void shouldEstimateConversationWithToolCalls() {
        List<ChatMessage> messages = List.of(
                UserMessage.from("What is the weather in Amsterdam?"),
                AiMessage.from(List.of(weatherRequest())),
                ToolExecutionResultMessage.from(weatherRequest(), "sunny, 20C"));

        assertEquals(38, estimator.estimateTokenCountInMessages(messages));
    }

    @Test
    public void shouldCountTheToolCallEndMarkerOnlyForASecondToolCall() {
        // DeepSeek's official chat template closes the tool calls in the branch that renders a subsequent tool call,
        // so a conversation with a single tool call has no such marker.
        List<ChatMessage> oneToolCall = List.of(
                UserMessage.from("What is the weather in Amsterdam?"),
                AiMessage.from(List.of(weatherRequest())));
        List<ChatMessage> twoToolCalls = List.of(
                UserMessage.from("What is the weather in Amsterdam?"),
                AiMessage.from(List.of(weatherRequest(), weatherRequest())));

        assertEquals(28, estimator.estimateTokenCountInMessages(oneToolCall));
        assertEquals(48, estimator.estimateTokenCountInMessages(twoToolCalls));
    }

    @Test
    public void shouldCountFewerChineseTokensThanTheRoughEstimateOfTheApiDocumentation() {
        String chinese = "今天天气怎么样？".repeat(20);

        // DeepSeek's API documentation suggests 0.6 token per Chinese character, the real tokenizer needs 0.5.
        assertEquals(80, estimator.estimateTokenCountInText(chinese));
    }

    @Test
    public void shouldFailForMultimodalContent() {
        UserMessage message = UserMessage.from(ImageContent.from("https://example.com/image.png"));

        assertThrows(
                IllegalArgumentException.class,
                () -> estimator.estimateTokenCountInMessage(message));
    }

    private static ToolExecutionRequest weatherRequest() {
        return ToolExecutionRequest.builder()
                .id("1")
                .name("getWeather")
                .arguments("{\"city\":\"Amsterdam\"}")
                .build();
    }
}
