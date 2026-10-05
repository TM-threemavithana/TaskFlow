package com.taskflow.model;

/**
 * Represents a User in the system.
 */
public class User {
    // 1. ENCAPSULATION
    // Fields are private to prevent unauthorized or accidental modifications.
    private Long id;
    private String name;
    private String email;

    // 2. CONSTRUCTOR
    // Required to provide initial data when the User is created.
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // 3. GETTERS AND SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // 4. OVERRIDING toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
