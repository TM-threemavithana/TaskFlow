package com.taskflow.model;

/**
 * A Class is like a blueprint. This Project class defines what a Project IS (its properties)
 * and what it can DO (its methods).
 */
public class Project {
    // 1. ENCAPSULATION / FIELDS (State)
    // We use 'private' so these variables cannot be changed directly from outside this class.
    // This protects the integrity of the object's state.
    private Long id;
    private String name;
    private String description;

    // 2. CONSTRUCTOR
    // The constructor is a special method called exactly once when the object is created using 'new Project(...)'.
    // It is used to set up the initial state of the newly created object.
    public Project(Long id, String name, String description) {
        // 'this.id' refers to the private field above.
        // 'id' refers to the parameter passed into the constructor.
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // 3. GETTERS and SETTERS (Accessors and Mutators)
    // Because our fields are private (Encapsulation), we provide public methods to read (get) 
    // and modify (set) them. This allows us to add validation logic here later if needed.
    
    public Long getId() {
        return id; // Read access
    }

    public void setId(Long id) {
        this.id = id; // Write access
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 4. METHOD OVERRIDING (Object Class)
    // Every class in Java automatically inherits from a built-in class called 'Object'.
    // 'Object' has a default toString() method. We are "overriding" (replacing) it here
    // so that printing a Project gives us readable JSON-like text instead of a memory address.
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
