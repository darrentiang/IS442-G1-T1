package com.studybuddy.model;

/** The group size a student prefers: one buddy, a small group, or no preference. */
public enum GroupFormat {

    ONE_TO_ONE("One-to-one"),
    SMALL_GROUP("Small group"),
    EITHER("One-to-one or small group");

    private final String label;

    /** Creates a value with the text shown to people. */
    GroupFormat(String label) {
        this.label = label;
    }

    /** Returns the text shown to people, e.g. "Small group". */
    public String getLabel() {
        return label;
    }

    /** Returns the GroupFormat whose name() is exactly this text, or null for null, blank or unknown text. */
    public static GroupFormat fromName(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (GroupFormat value : values()) {
            if (value.name().equals(text)) {
                return value;
            }
        }
        return null;
    }
}
