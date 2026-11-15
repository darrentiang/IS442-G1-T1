package com.studybuddy.model;

/** How a student prefers to meet: in person, online, or no preference. */
public enum StudyMode {

    /** Meets face to face. */
    IN_PERSON("In person"),
    /** Meets online. */
    ONLINE("Online"),
    /** No preference between in person and online. */
    EITHER("In person or online");

    private final String label;

    /** Creates a value with the text shown to people. */
    StudyMode(String label) {
        this.label = label;
    }

    /** Returns the text shown to people, e.g. "In person". */
    public String getLabel() {
        return label;
    }

    /** Returns the StudyMode whose name() is exactly this text, or null for null, blank or unknown text. */
    public static StudyMode fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (StudyMode value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
