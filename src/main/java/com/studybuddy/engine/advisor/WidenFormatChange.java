package com.studybuddy.engine.advisor;

import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.StudentProfile;

/** Widens the student's group size to Either, so both one-to-one and small-group students can match. */
public class WidenFormatChange extends PreferenceChange {

    private final GroupFormat current;

    /** Creates the change for a student whose group size is currently the given one (may be null). */
    public WidenFormatChange(GroupFormat current) {
        this.current = current;
    }

    /** Returns "format:EITHER". */
    @Override
    public String getKey() {
        return "format:" + GroupFormat.EITHER.name();
    }

    /** Applies unless the group size is already Either. */
    @Override
    public boolean appliesTo(StudentProfile profile) {
        return profile.getPreferences() != null && profile.getPreferences().getFormat() != GroupFormat.EITHER;
    }

    /** Sets the group size to Either. */
    @Override
    public void applyTo(StudentProfile profile) {
        profile.getPreferences().setFormat(GroupFormat.EITHER);
    }

    /** Returns e.g. "Change group size from One-to-one to One-to-one or small group". */
    @Override
    public String describe() {
        if (current == null) {
            return "Set your group size to " + GroupFormat.EITHER.getLabel();
        }
        return "Change group size from " + current.getLabel() + " to " + GroupFormat.EITHER.getLabel();
    }

    /** Returns "Changed group size to One-to-one or small group". */
    @Override
    public String getDoneMessage() {
        return "Changed group size to " + GroupFormat.EITHER.getLabel();
    }

    /** Returns which kind of session you may now be invited to. */
    @Override
    public String getTradeOff() {
        if (current == GroupFormat.ONE_TO_ONE) {
            return "You may be invited to study in small groups, not only one-to-one.";
        }
        if (current == GroupFormat.SMALL_GROUP) {
            return "You may be matched one-to-one, not only in small groups.";
        }
        return null;
    }

    /** Returns "group size". */
    @Override
    public String getBlockerLabel() {
        return "group size";
    }
}
