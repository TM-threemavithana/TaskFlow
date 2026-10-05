package com.taskflow.learning;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import com.taskflow.pattern.CommandPatternDemo;
import com.taskflow.pattern.CommandPatternDemo.ChangeStatusCommand;
import com.taskflow.pattern.CommandPatternDemo.MutableTask;
import com.taskflow.pattern.CommandPatternDemo.RenameCommand;
import com.taskflow.pattern.NotificationFactory;
import com.taskflow.pattern.ReportGenerator;
import com.taskflow.pattern.TextFormatterDemo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * LEARNING MAIN - a standalone program (plain Java, no Spring) that demonstrates every
 * Java concept requested, with detailed comments. Run it from the IDE or command line:
 * <pre>
 *   javac -d target/classes -sourcepath src/main/java src/main/java/com/taskflow/learning/LearningMain.java
 *   java -cp target/classes com.taskflow.learning.LearningMain
 * </pre>
 *
 * <p>Or just right-click → Run in your IDE. It does NOT start the Spring Boot server.
 */
public class LearningMain {

    // ============================================================================================
    //  1. OOP PILLAR: ENCAPSULATION (already shown in Phase 1 model classes - see Task.java, User.java)
    //     Private fields + public getters/setters = controlled access to an object's state.
    // ============================================================================================

    // ============================================================================================
    //  2. OOP PILLAR: INHERITANCE + POLYMORPHISM
    //
    //  INHERITANCE: "IS-A" relationship. A Dog IS-A Animal. A Square IS-A Shape.
    //  A subclass inherits fields and methods from the superclass and can add its own.
    //
    //  POLYMORPHISM: "many forms". A variable of type Animal can hold a Dog, Cat, or Bird.
    //  When you call animal.speak(), Java decides at RUNTIME which version to invoke - this is
    //  called dynamic dispatch.
    // ============================================================================================

    /** Base class for demonstration. */
    static abstract class Animal {
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        /** ABSTRACT METHOD: every subclass MUST provide its own implementation. */
        abstract String speak();

        /**
         * OVERRIDING toString(): we replace the Object class's default implementation.
         * This is OVERRIDING: same method name, same parameter list, in a subclass.
         */
        @Override
        public String toString() {
            return getClass().getSimpleName() + "{name='" + name + "'}";
        }
    }

    static class Dog extends Animal {
        Dog(String name) { super(name); }

        @Override
        String speak() { return getName() + " says: Woof!"; }   // OVERRIDING: provides Dog-specific behavior
    }

    static class Cat extends Animal {
        Cat(String name) { super(name); }

        @Override
        String speak() { return getName() + " says: Meow!"; }   // Different implementation = POLYMORPHISM
    }

    // ============================================================================================
    //  3. OOP PILLAR: ABSTRACTION
    //
    //  ABSTRACTION = hiding complexity. We define WHAT an object can do (its interface)
    //  without specifying HOW (the implementation). Abstract classes and interfaces both achieve this.
    // ============================================================================================

    // ============================================================================================
    //  4. OVERLOADING vs OVERRIDING
    //
    //  OVERLOADING: SAME method name, DIFFERENT parameter lists, in the SAME class.
    //  Resolved at COMPILE time (static binding).
    //
    //  OVERRIDING: SAME method name, SAME parameter list, in a SUBCLASS.
    //  Resolved at RUNTIME (dynamic dispatch / polymorphism).
    // ============================================================================================

    static class Calculator {
        // OVERLOADING: three methods named "add", each with different parameters.
        // The compiler picks the right one based on the argument types.

        /** add(int, int) - adds two integers. */
        int add(int a, int b) {
            return a + b;
        }

        /** add(int, int, int) - adds three integers. SAME NAME, different parameter COUNT. */
        int add(int a, int b, int c) {
            return a + b + c;
        }

        /** add(double, double) - adds two doubles. SAME NAME, different parameter TYPES. */
        double add(double a, double b) {
            return a + b;
        }
    }

    // ============================================================================================
    //  5. INTERFACE vs ABSTRACT CLASS
    //
    //  See the detailed comparison in ReportGenerator.java. Summary:
    //    - INTERFACE = a contract (what), multiple interfaces can be implemented.
    //    - ABSTRACT CLASS = partial implementation (how), single inheritance only.
    // ============================================================================================

    /** INTERFACE: defines a contract. Any class can implement it. */
    interface Describable {
        String describe();   // implicitly public abstract
    }

    /** INTERFACE with DEFAULT METHOD (Java 8+): provides a default implementation that
     *  implementors can optionally override. */
    interface Prioritizable {
        Priority getPriority();

        default boolean isUrgent() {
            return getPriority() == Priority.HIGH;
        }
    }

    /** A class can implement MULTIPLE interfaces (but extend only ONE class). */
    static class SimpleTask implements Describable, Prioritizable {
        private final String title;
        private final Priority priority;

        SimpleTask(String title, Priority priority) {
            this.title = title;
            this.priority = priority;
        }

        @Override
        public String describe() { return "Task: " + title; }

        @Override
        public Priority getPriority() { return priority; }
    }

    // ============================================================================================
    //  6. COMPOSITION ("HAS-A" relationship)
    //
    //  Favor composition over inheritance. Instead of "TaskList IS-A ArrayList<Task>", we say
    //  "TaskList HAS-A list of tasks" and delegate to it. This is more flexible.
    // ============================================================================================

    static class TaskBoard {
        // COMPOSITION: TaskBoard HAS-A list and a name. It is NOT a List.
        private final String boardName;
        private final List<SimpleTask> tasks = new ArrayList<>();

        TaskBoard(String boardName) {
            this.boardName = boardName;
        }

        void addTask(SimpleTask task) {
            tasks.add(task);
        }

        List<SimpleTask> getUrgentTasks() {
            // STREAM: a functional pipeline for transforming collections (see section 9)
            return tasks.stream()
                    .filter(Prioritizable::isUrgent)   // METHOD REFERENCE to a default interface method!
                    .toList();
        }

        @Override
        public String toString() {
            return "TaskBoard{'" + boardName + "', tasks=" + tasks.size() + "}";
        }
    }

    // ============================================================================================
    //  7. CUSTOM EXCEPTION
    //
    //  Already demonstrated in com.taskflow.exception.*. Here's a minimal standalone example.
    // ============================================================================================

    static class InvalidTaskException extends RuntimeException {
        private final String field;

        InvalidTaskException(String field, String message) {
            super(message);          // RuntimeException stores the message
            this.field = field;
        }

        String getField() { return field; }
    }

    // ============================================================================================
    //  8. GENERICS
    //
    //  Generics let you write TYPE-SAFE, REUSABLE code.
    //  Without generics: List stores Objects, requires casting, and errors happen at runtime.
    //  With generics: List<String> guarantees only Strings, and the COMPILER catches mistakes.
    // ============================================================================================

    /** A generic container that can hold any type T. */
    static class Result<T> {
        private final boolean success;
        private final T data;
        private final String errorMessage;

        private Result(boolean success, T data, String errorMessage) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        /** STATIC FACTORY METHOD with bounded type: T can be any type. */
        static <T> Result<T> success(T data) {
            return new Result<>(true, data, null);
        }

        static <T> Result<T> failure(String errorMessage) {
            return new Result<>(false, null, errorMessage);
        }

        boolean isSuccess() { return success; }
        T getData() { return data; }
        String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            return success ? "Success{" + data + "}" : "Failure{" + errorMessage + "}";
        }
    }

    // ============================================================================================
    //  MAIN METHOD: runs all demos
    // ============================================================================================

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║       TaskFlow Learning Demos (Plain Java)        ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");

        demoInheritanceAndPolymorphism();
        demoOverloading();
        demoInterfacesAndComposition();
        demoCustomException();
        demoCollections();
        demoGenerics();
        demoStreams();
        demoOptional();
        demoDesignPatterns();

        System.out.println("\n✅ All demos completed!");
    }

    // ---- Individual demo methods ----

    private static void demoInheritanceAndPolymorphism() {
        System.out.println("=== INHERITANCE & POLYMORPHISM ===");

        // POLYMORPHISM: the variable type is Animal, but the actual objects are Dog and Cat
        List<Animal> animals = List.of(new Dog("Rex"), new Cat("Whiskers"), new Dog("Buddy"));

        // At runtime, Java calls the correct speak() implementation for each object
        for (Animal animal : animals) {
            System.out.println("  " + animal.speak());   // Dynamic dispatch in action!
        }
        System.out.println();
    }

    private static void demoOverloading() {
        System.out.println("=== OVERLOADING vs OVERRIDING ===");
        Calculator calc = new Calculator();

        System.out.println("  add(2, 3)       = " + calc.add(2, 3));         // calls add(int, int)
        System.out.println("  add(2, 3, 4)    = " + calc.add(2, 3, 4));     // calls add(int, int, int)
        System.out.println("  add(2.5, 3.5)   = " + calc.add(2.5, 3.5));   // calls add(double, double)

        // OVERRIDING was shown above: Dog.speak() OVERRIDES Animal.speak()
        System.out.println("  OVERRIDING: Dog.speak() overrides Animal.speak()\n");
    }

    private static void demoInterfacesAndComposition() {
        System.out.println("=== INTERFACES & COMPOSITION ===");

        SimpleTask t1 = new SimpleTask("Deploy to prod", Priority.HIGH);
        SimpleTask t2 = new SimpleTask("Update docs", Priority.LOW);
        SimpleTask t3 = new SimpleTask("Fix security bug", Priority.HIGH);

        // INTERFACE usage: both Describable and Prioritizable
        System.out.println("  " + t1.describe() + " | urgent=" + t1.isUrgent());
        System.out.println("  " + t2.describe() + " | urgent=" + t2.isUrgent());

        // COMPOSITION: TaskBoard HAS-A list of tasks
        TaskBoard board = new TaskBoard("Sprint 1");
        board.addTask(t1);
        board.addTask(t2);
        board.addTask(t3);
        System.out.println("  " + board);
        System.out.println("  Urgent tasks: " + board.getUrgentTasks().size() + "\n");
    }

    private static void demoCustomException() {
        System.out.println("=== CUSTOM EXCEPTION ===");
        try {
            String title = "";
            if (title.isBlank()) {
                throw new InvalidTaskException("title", "Title cannot be blank");
            }
        } catch (InvalidTaskException e) {
            System.out.println("  Caught: field='" + e.getField() + "', message='" + e.getMessage() + "'\n");
        }
    }

    private static void demoCollections() {
        System.out.println("=== COLLECTIONS FRAMEWORK ===");

        // LIST: ordered, allows duplicates, indexed access
        List<String> arrayList = new ArrayList<>(List.of("Task A", "Task B", "Task C"));
        arrayList.add("Task D");
        System.out.println("  ArrayList (indexed): " + arrayList.get(0));

        // LinkedList: doubly-linked, efficient insert/remove at ends
        LinkedList<String> linkedList = new LinkedList<>(arrayList);
        linkedList.addFirst("Task Z");   // addFirst/getLast are LinkedList-specific in Java 17
        System.out.println("  LinkedList (first): " + linkedList.getFirst());

        // SET: unordered (or sorted), NO duplicates
        Set<String> set = new HashSet<>(arrayList);
        set.add("Task A");  // duplicate - ignored!
        System.out.println("  HashSet (no dupes, size after adding duplicate): " + set.size());

        // MAP: key-value pairs
        Map<String, Integer> taskCounts = new HashMap<>();
        taskCounts.put("TODO", 5);
        taskCounts.put("IN_PROGRESS", 3);
        taskCounts.put("COMPLETED", 8);
        System.out.println("  HashMap: " + taskCounts);

        // TreeMap: sorted by keys
        Map<String, Integer> sorted = new TreeMap<>(taskCounts);
        System.out.println("  TreeMap (sorted): " + sorted);

        // ITERATOR: the standard way to traverse ANY collection
        Iterator<String> it = arrayList.iterator();
        System.out.print("  Iterator: ");
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }

        // Sorting with Comparator (STRATEGY PATTERN!)
        List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));
        Collections.sort(names);                                      // natural order
        System.out.println("\n  Sorted (natural): " + names);
        names.sort(Comparator.reverseOrder());                         // reverse
        System.out.println("  Sorted (reverse): " + names);

        System.out.println();
    }

    private static void demoGenerics() {
        System.out.println("=== GENERICS ===");

        // Result<String> - the compiler ensures we only put Strings in
        Result<String> ok = Result.success("Task created successfully");
        Result<String> fail = Result.failure("Title is required");

        System.out.println("  " + ok);    // Success{Task created successfully}
        System.out.println("  " + fail);  // Failure{Title is required}

        // Result<Integer> - same class, different type!
        Result<Integer> count = Result.success(42);
        System.out.println("  " + count); // Success{42}

        // The COMPILER prevents: count.getData() + ok.getData() (Integer + String = error)
        System.out.println();
    }

    private static void demoStreams() {
        System.out.println("=== STREAMS & LAMBDAS ===");

        // Streams are a FUNCTIONAL pipeline: source → intermediate ops → terminal op.
        // They do NOT modify the original collection.

        List<String> tasks = List.of("Fix login bug", "Write tests", "Fix signup page",
                "Deploy to prod", "Fix typo in README");

        // FILTER: keep only elements matching a condition (Predicate)
        List<String> fixTasks = tasks.stream()
                .filter(t -> t.startsWith("Fix"))      // LAMBDA EXPRESSION: anonymous function
                .toList();
        System.out.println("  filter(startsWith 'Fix'): " + fixTasks);

        // MAP: transform each element
        List<String> uppercased = tasks.stream()
                .map(String::toUpperCase)              // METHOD REFERENCE: same as t -> t.toUpperCase()
                .toList();
        System.out.println("  map(toUpperCase): " + uppercased);

        // COLLECT: group elements
        Map<Character, List<String>> grouped = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.charAt(0)));  // group by first letter
        System.out.println("  groupingBy(first letter): " + grouped);

        // REDUCE: combine all elements into a single value
        int totalChars = tasks.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("  sum of lengths: " + totalChars);

        // CHAINING: filter → map → sort → collect
        List<String> result = tasks.stream()
                .filter(t -> t.length() > 10)
                .map(String::toLowerCase)
                .sorted()
                .toList();
        System.out.println("  chained (filter>map>sort): " + result);

        System.out.println();
    }

    private static void demoOptional() {
        System.out.println("=== OPTIONAL ===");

        // Optional<T> = a container that may or may not contain a value.
        // It forces you to handle the "absent" case, preventing NullPointerException.

        // Simulating a "find task by title" that might not find anything
        List<String> tasks = List.of("Fix bug", "Write docs", "Deploy");

        Optional<String> found = tasks.stream()
                .filter(t -> t.contains("bug"))
                .findFirst();
        System.out.println("  found 'bug': " + found);                    // Optional[Fix bug]
        System.out.println("  value: " + found.orElse("(not found)"));    // Fix bug

        Optional<String> missing = tasks.stream()
                .filter(t -> t.contains("refactor"))
                .findFirst();
        System.out.println("  found 'refactor': " + missing);              // Optional.empty
        System.out.println("  value: " + missing.orElse("(not found)"));   // (not found)

        // orElseThrow: throw an exception if empty
        // found.orElseThrow(() -> new ResourceNotFoundException("Task", "title", "refactor"));

        // map: transform the value inside the Optional (if present)
        String upper = found.map(String::toUpperCase).orElse("N/A");
        System.out.println("  map to upper: " + upper);

        // ifPresent: execute code only if value exists
        found.ifPresent(t -> System.out.println("  ifPresent: found task '" + t + "'"));

        System.out.println();
    }

    private static void demoDesignPatterns() {
        System.out.println("=== DESIGN PATTERNS (standalone demos) ===\n");

        // ----- FACTORY -----
        System.out.println("--- Factory Pattern ---");
        var notification = NotificationFactory.createTaskNotification(
                "COMPLETED", "Fix login bug", Priority.HIGH, TaskStatus.COMPLETED);
        System.out.println("  " + notification);

        // ----- DECORATOR -----
        System.out.println("\n--- Decorator Pattern ---");
        System.out.println("  " + TextFormatterDemo.demo());

        // ----- COMMAND with UNDO -----
        System.out.println("\n--- Command Pattern (undo/redo) ---");
        MutableTask task = new MutableTask("Fix bug", "TODO");
        System.out.println("  Initial: " + task);

        // Execute commands
        CommandPatternDemo.Command cmd1 = new ChangeStatusCommand(task, "IN_PROGRESS");
        cmd1.execute();
        System.out.println("  After execute: " + task + " | " + cmd1.describe());

        CommandPatternDemo.Command cmd2 = new RenameCommand(task, "Fix critical bug");
        cmd2.execute();
        System.out.println("  After rename: " + task + " | " + cmd2.describe());

        // Undo both commands (in reverse order!)
        cmd2.undo();
        System.out.println("  After undo rename: " + task);
        cmd1.undo();
        System.out.println("  After undo status: " + task);

        // ----- TEMPLATE METHOD -----
        System.out.println("\n--- Template Method Pattern ---");
        Map<String, Object> data = new HashMap<>();
        data.put("Total Tasks", 15);
        data.put("Completed", 8);
        data.put("Overdue", 2);

        ReportGenerator textGen = new ReportGenerator.TextReportGenerator();
        System.out.println(textGen.generateReport("Task Summary", data));

        ReportGenerator htmlGen = new ReportGenerator.HtmlReportGenerator();
        System.out.println(htmlGen.generateReport("Task Summary", data));
    }
}
