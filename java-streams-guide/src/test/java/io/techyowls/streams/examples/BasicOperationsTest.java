package io.techyowls.streams.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BasicOperationsTest {

    private BasicOperations operations;

    @BeforeEach
    void setUp() {
        operations = new BasicOperations();
    }

    @Test
    void filterLongNames() {
        List<String> names = List.of("Al", "Bob", "Charlie", "Diana", "Ed");
        List<String> result = operations.filterLongNames(names, 4);
        assertEquals(List.of("Charlie", "Diana"), result);
    }

    @Test
    void toUpperCase() {
        List<String> names = List.of("hello", "world");
        List<String> result = operations.toUpperCase(names);
        assertEquals(List.of("HELLO", "WORLD"), result);
    }

    @Test
    void flattenLists() {
        List<List<String>> nested = List.of(
            List.of("a", "b"),
            List.of("c", "d", "e"),
            List.of("f")
        );
        List<String> result = operations.flattenLists(nested);
        assertEquals(List.of("a", "b", "c", "d", "e", "f"), result);
    }

    @Test
    void extractWords() {
        List<String> sentences = List.of("Hello world", "Java streams are great");
        List<String> result = operations.extractWords(sentences);
        assertEquals(List.of("Hello", "world", "Java", "streams", "are", "great"), result);
    }

    @Test
    void sumNumbers() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        int result = operations.sumNumbers(numbers);
        assertEquals(15, result);
    }

    @Test
    void findMax() {
        List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6);
        Optional<Integer> result = operations.findMax(numbers);
        assertTrue(result.isPresent());
        assertEquals(9, result.get());
    }

    @Test
    void sumOfSquaresOfEvens() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
        int result = operations.sumOfSquaresOfEvens(numbers);
        // 2^2 + 4^2 + 6^2 = 4 + 16 + 36 = 56
        assertEquals(56, result);
    }

    @Test
    void uniqueSorted() {
        List<String> items = List.of("banana", "apple", "cherry", "apple", "banana");
        List<String> result = operations.uniqueSorted(items);
        assertEquals(List.of("apple", "banana", "cherry"), result);
    }

    @Test
    void paginate() {
        List<String> items = List.of("a", "b", "c", "d", "e", "f", "g");

        assertEquals(List.of("a", "b", "c"), operations.paginate(items, 0, 3));
        assertEquals(List.of("d", "e", "f"), operations.paginate(items, 1, 3));
        assertEquals(List.of("g"), operations.paginate(items, 2, 3));
    }
}
