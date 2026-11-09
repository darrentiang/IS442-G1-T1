package com.studybuddy.engine.simulation;

import com.studybuddy.model.Student;

/** One student's chance of ending with a buddy they can meet, under the saved and the proposed config. */
public class StudentChance {

    private final Student student;
    private final double savedChance;
    private final double proposedChance;

    /**
     * Creates the pair of chances for one student.
     *
     * @param student the student
     * @param savedChance chance under the saved config, 0 to 1
     * @param proposedChance chance under the proposed config, 0 to 1
     */
    public StudentChance(Student student, double savedChance, double proposedChance) {
        this.student = student;
        this.savedChance = savedChance;
        this.proposedChance = proposedChance;
    }

    /** Returns the student. */
    public Student getStudent() {
        return student;
    }

    /** Returns the chance under the saved config, 0 to 1. */
    public double getSavedChance() {
        return savedChance;
    }

    /** Returns the chance under the proposed config, 0 to 1. */
    public double getProposedChance() {
        return proposedChance;
    }
}
