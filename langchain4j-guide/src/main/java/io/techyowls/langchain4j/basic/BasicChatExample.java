package io.techyowls.langchain4j.basic;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Basic chat example - AI in Java in 3 lines.
 */
public class BasicChatExample {

    public static void main(String[] args) {
        // 1. Create a model
        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o")
            .build();

        // 2. Chat
        String response = model.generate("Explain virtual threads in Java 21 in 2 sentences.");

        // 3. Done
        System.out.println(response);
    }
}
