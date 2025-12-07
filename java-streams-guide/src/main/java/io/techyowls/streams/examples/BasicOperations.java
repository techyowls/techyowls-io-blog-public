package io.techyowls.streams.examples;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Basic stream operations: filter, map, flatMap, reduce
 */
public class BasicOperations {

    // Filter - keep elements matching predicate
    public List<String> filterLongNames(List<String> names, int minLength) {
        return names.stream()
            .filter(name -> name.length() >= minLength)
            .toList();
    }

    // Map - transform each element
    public List<String> toUpperCase(List<String> names) {
        return names.stream()
            .map(String::toUpperCase)
            .toList();
    }

    // FlatMap - flatten nested structures
    public List<String> flattenLists(List<List<String>> nested) {
        return nested.stream()
            .flatMap(List::stream)
            .toList();
    }

    // Extract words from sentences
    public List<String> extractWords(List<String> sentences) {
        return sentences.stream()
            .flatMap(sentence -> Stream.of(sentence.split("\\s+")))
            .filter(word -> !word.isBlank())
            .toList();
    }

    // Reduce - combine elements into single result
    public int sumNumbers(List<Integer> numbers) {
        return numbers.stream()
            .reduce(0, Integer::sum);
    }

    // Reduce with identity
    public String concatenate(List<String> strings) {
        return strings.stream()
            .reduce("", (a, b) -> a + b);
    }

    // Reduce to find max
    public Optional<Integer> findMax(List<Integer> numbers) {
        return numbers.stream()
            .reduce(Integer::max);
    }

    // Chaining operations
    public int sumOfSquaresOfEvens(List<Integer> numbers) {
        return numbers.stream()
            .filter(n -> n % 2 == 0)        // keep evens
            .map(n -> n * n)                 // square each
            .reduce(0, Integer::sum);        // sum all
    }

    // Distinct and sorted
    public List<String> uniqueSorted(List<String> items) {
        return items.stream()
            .distinct()
            .sorted()
            .toList();
    }

    // Limit and skip for pagination
    public List<String> paginate(List<String> items, int page, int size) {
        return items.stream()
            .skip((long) page * size)
            .limit(size)
            .toList();
    }

    // Peek for debugging (side effects)
    public List<Integer> processWithLogging(List<Integer> numbers) {
        return numbers.stream()
            .peek(n -> System.out.println("Before filter: " + n))
            .filter(n -> n > 0)
            .peek(n -> System.out.println("After filter: " + n))
            .map(n -> n * 2)
            .peek(n -> System.out.println("After map: " + n))
            .toList();
    }
}
