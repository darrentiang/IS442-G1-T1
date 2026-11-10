package com.studybuddy.engine.advisor;

/** A preference that held back some near misses, and the change that would relax it. */
public class Blocker {

    private final PreferenceChange change;
    private final int people;

    /**
     * Creates the blocker.
     *
     * @param change the change that relaxes this preference
     * @param people how many near misses this preference cost the most points
     */
    public Blocker(PreferenceChange change, int people) {
        this.change = change;
        this.people = people;
    }

    /** Returns the change that relaxes this preference. */
    public PreferenceChange getChange() {
        return change;
    }

    /** Returns how many near misses this preference held back. */
    public int getPeople() {
        return people;
    }
}
