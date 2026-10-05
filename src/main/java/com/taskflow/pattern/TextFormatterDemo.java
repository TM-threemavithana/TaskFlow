package com.taskflow.pattern;

/**
 * DESIGN PATTERN: DECORATOR (structural) - standalone demo.
 *
 * <p>PROBLEM: We want to format task descriptions in different ways (Markdown bold, uppercase,
 * add a timestamp prefix). Creating a subclass for each combination leads to a class explosion:
 * BoldUppercaseTimestampFormatter, BoldTimestampFormatter, UppercaseFormatter...
 * With N decorators that would be 2^N classes!
 *
 * <p>DECORATOR SOLUTION: Wrap an object with another object that adds behavior,
 * then wrap THAT with another, and so on. Each wrapper (decorator) adds one piece of behavior
 * and delegates the rest to the wrapped object. They can be stacked in any combination at runtime.
 *
 * <p>Real-world examples: Java's I/O streams are decorators:
 * {@code new BufferedReader(new InputStreamReader(new FileInputStream("file.txt")))}
 * Each wrapper adds a capability (buffering, character decoding, file reading).
 *
 * <p>USED IN THE REAL APP: Spring's filter chain (JwtAuthenticationFilter wrapping around
 * the next filter) is conceptually a chain of decorators.
 */
public final class TextFormatterDemo {

    private TextFormatterDemo() {
    }

    // ---- The Component interface ----
    /** Functional interface: strategy for formatting text. */
    @FunctionalInterface
    public interface TextFormatter {
        String format(String text);
    }

    // ---- Concrete component ----
    /** Does nothing: returns text as-is. This is the "base" that decorators wrap around. */
    public static final TextFormatter PLAIN = text -> text;

    // ---- Decorators (each wraps another TextFormatter) ----

    /** Adds Markdown bold markers. */
    public static TextFormatter bold(TextFormatter inner) {
        return text -> "**" + inner.format(text) + "**";
    }

    /** Converts to uppercase. */
    public static TextFormatter uppercase(TextFormatter inner) {
        return text -> inner.format(text).toUpperCase();
    }

    /** Adds a prefix with a tag. */
    public static TextFormatter withPrefix(TextFormatter inner, String prefix) {
        return text -> "[" + prefix + "] " + inner.format(text);
    }

    /**
     * Demonstrate stacking decorators:
     * <pre>
     *   withPrefix(bold(uppercase(PLAIN)), "TASK")
     *   "hello" → "HELLO" → "**HELLO**" → "[TASK] **HELLO**"
     * </pre>
     */
    public static String demo() {
        TextFormatter formatter = withPrefix(bold(uppercase(PLAIN)), "TASK");
        return formatter.format("fix login bug");  // → "[TASK] **FIX LOGIN BUG**"
    }
}
