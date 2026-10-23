package dev.langchain4j.examples.shared;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that DeepSeek's official {@code chat_template} is rendered the way the DeepSeek API renders it.
 */
public class DeepSeekChatTemplateTest {

    private final DeepSeekChatTemplate template = DeepSeekChatTemplate.load();

    @Test
    public void shouldRenderSystemUserAndAssistantMessages() {
        List<ChatMessage> messages = List.of(
                SystemMessage.from("system"),
                UserMessage.from("hello"),
                AiMessage.from("你好"));

        assertEquals(
                "<｜begin▁of▁sentence｜>system<｜User｜>hello<｜Assistant｜>你好<｜end▁of▁sentence｜>",
                template.render(messages));
    }

    @Test
    public void shouldMergeSystemMessagesWithABlankLine() {
        List<ChatMessage> messages = List.of(
                SystemMessage.from("first"),
                SystemMessage.from("second"),
                UserMessage.from("hello"));

        assertEquals(
                "<｜begin▁of▁sentence｜>first\n\nsecond<｜User｜>hello",
                template.render(messages));
    }

    @Test
    public void shouldRenderToolCallAndToolOutput() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name("getWeather")
                .arguments("{\"city\":\"Amsterdam\"}")
                .build();

        List<ChatMessage> messages = List.of(
                UserMessage.from("weather?"),
                AiMessage.from(List.of(request)),
                ToolExecutionResultMessage.from(request, "sunny"));

        assertEquals(
                "<｜begin▁of▁sentence｜><｜User｜>weather?"
                        + "<｜Assistant｜><｜tool▁calls▁begin｜><｜tool▁call▁begin｜>"
                        + "function<｜tool▁sep｜>getWeather\n```json\n{\"city\":\"Amsterdam\"}\n```"
                        + "<｜tool▁call▁end｜>"
                        + "<｜tool▁outputs▁begin｜><｜tool▁output▁begin｜>sunny<｜tool▁output▁end｜>"
                        + "<｜tool▁outputs▁end｜>",
                template.render(messages));
    }

    @Test
    public void shouldRenderTheAnswerThatFollowsAToolOutput() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name("getWeather")
                .arguments("{}")
                .build();

        List<ChatMessage> messages = List.of(
                UserMessage.from("weather?"),
                ToolExecutionResultMessage.from(request, "sunny"),
                AiMessage.from("It is sunny."));

        assertEquals(
                "<｜begin▁of▁sentence｜><｜User｜>weather?"
                        + "<｜tool▁outputs▁begin｜><｜tool▁output▁begin｜>sunny<｜tool▁output▁end｜>"
                        + "<｜tool▁outputs▁end｜>It is sunny.<｜end▁of▁sentence｜>",
                template.render(messages));
    }

    @Test
    public void shouldRenderEveryToolCallOfOneMessage() {
        ToolExecutionRequest weather = ToolExecutionRequest.builder()
                .id("1")
                .name("getWeather")
                .arguments("{}")
                .build();
        ToolExecutionRequest time = ToolExecutionRequest.builder()
                .id("2")
                .name("getTime")
                .arguments("{}")
                .build();

        List<ChatMessage> messages =
                List.of(UserMessage.from("weather and time?"), AiMessage.from(List.of(weather, time)));

        assertEquals(
                "<｜begin▁of▁sentence｜><｜User｜>weather and time?"
                        + "<｜Assistant｜><｜tool▁calls▁begin｜><｜tool▁call▁begin｜>"
                        + "function<｜tool▁sep｜>getWeather\n```json\n{}\n```<｜tool▁call▁end｜>"
                        + "\n<｜tool▁call▁begin｜>function<｜tool▁sep｜>getTime\n```json\n{}\n```"
                        + "<｜tool▁call▁end｜><｜tool▁calls▁end｜><｜end▁of▁sentence｜>",
                template.render(messages));
    }

    @Test
    public void shouldJoinSeveralTextPartsOfAUserMessageWithALineFeed() {
        List<ChatMessage> messages =
                List.of(UserMessage.from(TextContent.from("first"), TextContent.from("second")));

        assertEquals(
                "<｜begin▁of▁sentence｜><｜User｜>first\nsecond",
                template.render(messages));
    }
}
