package io.techyowls.structuredoutput.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import org.springframework.ai.converter.StructuredOutputConverter;

import java.util.Map;

/**
 * Custom converter for typed maps: Map<String, V>
 *
 * Spring AI's MapOutputConverter only supports Map<String, Object>.
 * This allows you to get Map<String, Character>, Map<String, Product>, etc.
 */
public class GenericMapOutputConverter<V> implements StructuredOutputConverter<Map<String, V>> {

    private final ObjectMapper objectMapper;
    private final String jsonSchema;
    private final TypeReference<Map<String, V>> typeRef;

    public GenericMapOutputConverter(Class<V> valueType) {
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.typeRef = new TypeReference<>() {};
        this.jsonSchema = generateJsonSchema(valueType);
    }

    @Override
    public Map<String, V> convert(String text) {
        try {
            // Strip markdown code blocks if present
            text = text.replaceAll("^```json\\s*", "")
                       .replaceAll("\\s*```$", "")
                       .trim();
            return objectMapper.readValue(text, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFormat() {
        return """
            Your response should be in JSON format.
            The data structure should be a Map where keys are strings
            and values match this schema:
            %s
            Do not include explanations or markdown code blocks.
            Return only valid JSON.
            """.formatted(jsonSchema);
    }

    private String generateJsonSchema(Class<V> valueType) {
        SchemaGeneratorConfig config = new SchemaGeneratorConfigBuilder(
            SchemaVersion.DRAFT_2020_12,
            OptionPreset.PLAIN_JSON
        ).build();

        SchemaGenerator generator = new SchemaGenerator(config);
        return generator.generateSchema(valueType).toString();
    }
}
