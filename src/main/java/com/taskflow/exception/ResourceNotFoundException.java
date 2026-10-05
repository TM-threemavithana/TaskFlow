package com.taskflow.exception;

/**
 * CUSTOM EXCEPTION for "404 Not Found" scenarios.
 *
 * <p>WHY CUSTOM EXCEPTIONS?
 * Java's built-in exceptions ({@code NullPointerException}, {@code IllegalArgumentException} ...) are
 * generic. A custom exception lets us:
 * <ol>
 *   <li>Give it a meaningful name that documents the error scenario.</li>
 *   <li>Carry extra data (here: which resource, which field, what value was searched for).</li>
 *   <li>Catch it centrally in {@code GlobalExceptionHandler} and return a proper HTTP 404 response.</li>
 * </ol>
 *
 * <p>We extend {@code RuntimeException} (an UNCHECKED exception) so callers are not forced to write
 * try-catch. The global handler catches it instead.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Example: {@code throw new ResourceNotFoundException("Task", "id", 42)}
     * produces the message: "Task not found with id : 42"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}
