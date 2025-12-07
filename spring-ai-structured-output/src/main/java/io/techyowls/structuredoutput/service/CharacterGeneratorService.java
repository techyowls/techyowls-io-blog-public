package io.techyowls.structuredoutput.service;

import io.techyowls.structuredoutput.model.Character;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Generate D&D characters as typed Java objects.
 */
@Service
public class CharacterGeneratorService {

    private final ChatClient chatClient;

    public CharacterGeneratorService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    /**
     * Generate a single character using BeanOutputConverter (high-level API).
     */
    public Character generateCharacter(String race) {
        return chatClient.prompt()
            .user("Generate a D&D character who is a " + race + " wizard. " +
                  "Include name, age (appropriate for race), race, characterClass, and a 2-sentence bio.")
            .call()
            .entity(Character.class);
    }

    /**
     * Generate multiple characters.
     */
    public List<Character> generateParty(int count) {
        String prompt = """
            Generate %d different D&D characters for an adventuring party.
            Include diverse races and classes.
            Each character should have: name, age, race, characterClass, and a 2-sentence bio.
            """.formatted(count);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(new org.springframework.core.ParameterizedTypeReference<List<Character>>() {});
    }
}
