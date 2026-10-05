package com.taskflow;

import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.model.Project;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TaskFlow Phase 1: Classes & Objects ---");

        // 1. CREATING OBJECTS (Instantiation)
        // 'User' is the class (blueprint). 'user1' is the object (the actual house built from the blueprint).
        // The 'new' keyword allocates memory and calls the Constructor.
        User user1 = new User(1L, "Alice", "alice@example.com");
        
        Project project1 = new Project(1L, "Website Redesign", "Overhaul the main company website");
        
        // Notice we only pass 'id' and 'title'. The constructor handles defaults for status and priority.
        Task task1 = new Task(1L, "Design Mockups");

        // 2. PRINTING STATE
        // When we print an object, Java automatically calls its toString() method.
        System.out.println("Initial States:");
        System.out.println(user1);
        System.out.println(project1);
        System.out.println(task1);

        // 3. ENCAPSULATION IN ACTION
        // We use the setter to safely update the description
        task1.setDescription("Create Figma mockups for the landing page");
        
        // We invoke behavior on the object directly. We don't change its internal variables manually.
        task1.complete(); // Changes internal status to COMPLETED

        System.out.println("\nAfter applying updates:");
        // Observe that the description and status have changed
        System.out.println(task1);
    }
}
