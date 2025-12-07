package io.techyowls.langchain4j.basic;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.Map;

/**
 * Using prompt templates for structured prompts.
 */
public class PromptTemplateExample {

    public static void main(String[] args) {
        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o")
            .temperature(0.7)
            .build();

        // Create a template
        PromptTemplate template = PromptTemplate.from(
            "Explain {{topic}} to a {{audience}} in {{style}} style. Keep it to 3 sentences."
        );

        // Apply variables
        Map<String, Object> vars = Map.of(
            "topic", "microservices",
            "audience", "Java developer",
            "style", "concise"
        );

        Prompt prompt = template.apply(vars);
        String answer = model.generate(prompt.text());

        System.out.println("Prompt: " + prompt.text());
        System.out.println("\nAnswer: " + answer);
    }
}
