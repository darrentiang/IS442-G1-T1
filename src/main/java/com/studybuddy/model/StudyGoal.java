package com.studybuddy.model;

/** What a student wants to get out of studying together. */
public enum StudyGoal {

    CONCEPT_REVIEW("Concept review"),
    PROBLEM_SOLVING("Problem solving"),
    EXAM_PREP("Exam prep"),
    PROJECT_DISCUSSION("Project discussion");

    private final String label;

    /** Creates a value with the text shown to people. */
    StudyGoal(String label) {
        this.label = label;
    }

    /** Returns the text shown to people, e.g. "Exam prep". */
    public String getLabel() {
        return label;
    }

    /** Returns the StudyGoal whose name() is exactly this text, or null for null, blank or unknown text. */
    public static StudyGoal fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (StudyGoal value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
