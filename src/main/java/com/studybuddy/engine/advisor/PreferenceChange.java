package com.studybuddy.engine.advisor;

import com.studybuddy.model.StudentProfile;

/**
 * One change a student could make to their own preferences, such as widening their study mode.
 * BOSS Advisor treats every kind of change the same way: it checks the change applies, tries it on
 * a copy of the student, and asks the real matching engine what it would gain. Adding a new kind of
 * suggestion means adding one subclass; the advisor itself does not change.
 */
public abstract class PreferenceChange {

    /** Returns a short id for this exact change (e.g. "mode:EITHER"), sent by the Apply button. */
    public abstract String getKey();

    /** Returns true if the change makes sense for this profile (it is not already in place). */
    public abstract boolean appliesTo(StudentProfile profile);

    /** Makes the change on the given profile. */
    public abstract void applyTo(StudentProfile profile);

    /** Returns the exact instruction shown to the student, e.g. "Change study mode from In person to Either". */
    public abstract String describe();

    /** Returns what the toast says once the change is applied, e.g. "Changed study mode to Either". */
    public abstract String getDoneMessage();

    /** Returns what the student gives up by making the change, or null if nothing. */
    public abstract String getTradeOff();

    /** Returns the preference this change relaxes, in words, e.g. "study mode". */
    public abstract String getBlockerLabel();
}
