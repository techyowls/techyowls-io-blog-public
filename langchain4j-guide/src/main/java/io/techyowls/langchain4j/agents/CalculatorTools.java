package io.techyowls.langchain4j.agents;

import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom tools that the LLM can call.
 *
 * Each @Tool method becomes a function the AI can invoke.
 */
public class CalculatorTools {

    @Tool("Adds two numbers together")
    public int add(int a, int b) {
        System.out.println("[Tool] add(" + a + ", " + b + ")");
        return a + b;
    }

    @Tool("Multiplies two numbers")
    public int multiply(int a, int b) {
        System.out.println("[Tool] multiply(" + a + ", " + b + ")");
        return a * b;
    }

    @Tool("Subtracts second number from first")
    public int subtract(int a, int b) {
        System.out.println("[Tool] subtract(" + a + ", " + b + ")");
        return a - b;
    }

    @Tool("Divides first number by second")
    public double divide(int a, int b) {
        System.out.println("[Tool] divide(" + a + ", " + b + ")");
        if (b == 0) throw new IllegalArgumentException("Cannot divide by zero");
        return (double) a / b;
    }

    @Tool("Gets current date and time")
    public String currentDateTime() {
        System.out.println("[Tool] currentDateTime()");
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Tool("Calculates the square root of a number")
    public double squareRoot(double n) {
        System.out.println("[Tool] squareRoot(" + n + ")");
        if (n < 0) throw new IllegalArgumentException("Cannot calculate square root of negative number");
        return Math.sqrt(n);
    }
}
