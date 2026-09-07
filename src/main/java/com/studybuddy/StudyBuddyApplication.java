package com.studybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Study Buddy Matcher web application.
 */
@SpringBootApplication
public class StudyBuddyApplication {

    /**
     * Starts the embedded web server and the Spring application context.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        SpringApplication.run(StudyBuddyApplication.class, args);
    }
}
