package com.studybuddy.engine.advisor;

import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyMode;

/** Widens the student's study mode to Either, so both in-person and online students can match. */
public class WidenModeChange extends PreferenceChange {

    private final StudyMode current;

    /** Creates the change for a student whose mode is currently the given one (may be null). */
    public WidenModeChange(StudyMode current) {
        this.current = current;
    }

    /** Returns "mode:EITHER". */
    @Override
    public String getKey() {
        return "mode:" + StudyMode.EITHER.name();
    }

    /** Applies unless the mode is already Either. */
    @Override
    public boolean appliesTo(StudentProfile profile) {
        return profile.getPreferences() != null && profile.getPreferences().getMode() != StudyMode.EITHER;
    }

    /** Sets the study mode to Either. */
    @Override
    public void applyTo(StudentProfile profile) {
        profile.getPreferences().setMode(StudyMode.EITHER);
    }

    /** Returns e.g. "Change study mode from In person to In person or online". */
    @Override
    public String describe() {
        if (current == null) {
            return "Set your study mode to " + StudyMode.EITHER.getLabel();
        }
        return "Change study mode from " + current.getLabel() + " to " + StudyMode.EITHER.getLabel();
    }

    /** Returns "Changed study mode to In person or online". */
    @Override
    public String getDoneMessage() {
        return "Changed study mode to " + StudyMode.EITHER.getLabel();
    }

    /** Returns which kind of student you may now be matched with. */
    @Override
    public String getTradeOff() {
        if (current == StudyMode.IN_PERSON) {
            return "You may be matched with people who prefer to study online.";
        }
        if (current == StudyMode.ONLINE) {
            return "You may be matched with people who prefer to meet in person.";
        }
        return null;
    }

    /** Returns "study mode". */
    @Override
    public String getBlockerLabel() {
        return "study mode";
    }
}
