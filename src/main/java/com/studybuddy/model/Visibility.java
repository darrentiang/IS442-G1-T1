package com.studybuddy.model;

/** Whether a study group can be discovered by everyone or only by invitation. */
public enum Visibility {

    PUBLIC("Public: anyone can find it"),
    PRIVATE("Private: only people with the link");

    private final String label;

    /** Creates a value with the text shown to people. */
    Visibility(String label) {
        this.label = label;
    }

    /** Returns the text shown to people, e.g. "Public: anyone can find it". */
    public String getLabel() {
        return label;
    }

    /** Returns the Visibility whose name() is exactly this text, or null for null, blank or unknown text. */
    public static Visibility fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (Visibility value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
