package com.studybuddy.model;

/** How hard a student intends to study. */
public enum Intensity {

    CASUAL("Casual"),
    STEADY("Steady"),
    GRIND("Grind");

    private final String label;

    /** Creates a value with the text shown to people. */
    Intensity(String label) {
        this.label = label;
    }

    /** Returns the text shown to people, e.g. "Grind". */
    public String getLabel() {
        return label;
    }

    /** Returns the Intensity whose name() is exactly this text, or null for null, blank or unknown text. */
    public static Intensity fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (Intensity value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
