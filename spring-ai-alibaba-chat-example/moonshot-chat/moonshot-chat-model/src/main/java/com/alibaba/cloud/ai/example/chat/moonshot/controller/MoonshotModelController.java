package com.alibaba.cloud.ai.example.chat.moonshot.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.moonshot.MoonshotChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author kuilz
 * @date 2025/1/19
 */
@RestController
@RequestMapping("/moonshot/chat-model")
public class MoonshotModelController {
    private static final String DEFAULT_PROMPT = "你好，介绍下你自己吧。";

    private final ChatModel moonshotChatModel;

    public MoonshotModelController(ChatModel chatModel) {
        this.moonshotChatModel = chatModel;
    }

    /**
     * ChatModel 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return moonshotChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getContent();
    }

    /**
     * ChatModel 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        Flux<ChatResponse> chatResponseFlux = moonshotChatModel.stream(new Prompt(DEFAULT_PROMPT));
        return chatResponseFlux
                .flatMap(resp -> {
                    String content = null;
                    if (resp.getResult() != null && resp.getResult().getOutput() != null) {
                        content = resp.getResult().getOutput().getContent();
                    }
                    return content != null ? Mono.just(content) : Mono.empty();  // 如果 content 为 null，返回 empty
                });
    }

    /**
     * 使用编程方式自定义 LLMs ChatOptions 参数， {@link org.springframework.ai.moonshot.MoonshotChatOptions}
     * 优先级高于在 application.yml 中配置的 LLMs 参数！
     */
    @GetMapping("/custom/chat")
    public String customChat() {
        MoonshotChatOptions moonshotChatOptions = MoonshotChatOptions.builder()
                .withModel("moonshot-v1-32k")
                .withTopP(0.8)
                .withTemperature(0.8)
                .build();

        return moonshotChatModel.call(new Prompt(DEFAULT_PROMPT, moonshotChatOptions)).getResult().getOutput().getContent();
    }
}
