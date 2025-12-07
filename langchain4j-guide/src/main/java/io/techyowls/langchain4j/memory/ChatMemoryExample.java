package io.techyowls.langchain4j.memory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Chat memory - remember conversations.
 */
public class ChatMemoryExample {

    public static void main(String[] args) {
        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o")
            .build();

        // Create memory that keeps last 10 messages
        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        // First turn - introduce ourselves
        memory.add(UserMessage.from("My name is Alex and I'm learning Java 21."));
        AiMessage response1 = model.generate(memory.messages()).content();
        memory.add(response1);
        System.out.println("User: My name is Alex and I'm learning Java 21.");
        System.out.println("AI: " + response1.text());

        // Second turn - test memory
        memory.add(UserMessage.from("What's my name and what am I learning?"));
        AiMessage response2 = model.generate(memory.messages()).content();
        memory.add(response2);
        System.out.println("\nUser: What's my name and what am I learning?");
        System.out.println("AI: " + response2.text());

        // Third turn - continue conversation
        memory.add(UserMessage.from("What should I learn next?"));
        AiMessage response3 = model.generate(memory.messages()).content();
        System.out.println("\nUser: What should I learn next?");
        System.out.println("AI: " + response3.text());
    }
}
