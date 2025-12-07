package io.techyowls.langchain4j.agents;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Agent example - LLM that can use tools to take actions.
 *
 * The AI will automatically decide when to call tools based on the question.
 */
public class AgentExample {

    // Define the AI service interface
    interface MathAssistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        // Create the model
        var model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o")
            .build();

        // Build the agent with tools and memory
        MathAssistant assistant = AiServices.builder(MathAssistant.class)
            .chatLanguageModel(model)
            .tools(new CalculatorTools())
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .build();

        // Test 1: Simple calculation
        System.out.println("=== Test 1: Simple calculation ===");
        String result1 = assistant.chat("What is 42 multiplied by 17?");
        System.out.println("Answer: " + result1);

        // Test 2: Multi-step calculation
        System.out.println("\n=== Test 2: Multi-step calculation ===");
        String result2 = assistant.chat("What is 42 multiplied by 17, then add 100?");
        System.out.println("Answer: " + result2);

        // Test 3: Current time
        System.out.println("\n=== Test 3: Current time ===");
        String result3 = assistant.chat("What's the current date and time?");
        System.out.println("Answer: " + result3);

        // Test 4: Memory test
        System.out.println("\n=== Test 4: Memory test ===");
        String result4 = assistant.chat("Take the first result and divide it by 2");
        System.out.println("Answer: " + result4);
    }
}
