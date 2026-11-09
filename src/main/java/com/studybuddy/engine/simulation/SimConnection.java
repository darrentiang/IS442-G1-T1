package com.studybuddy.engine.simulation;

/** A pair of buddies in a simulation, with how much usable time they share. */
public class SimConnection {

    private final int usableHours;

    /**
     * Creates a connection.
     *
     * @param usableHours hours a week they are both free in blocks long enough for a session
     */
    public SimConnection(int usableHours) {
        this.usableHours = usableHours;
    }

    /** Returns true if the pair share at least one free block long enough for a session. */
    public boolean canMeet() {
        return usableHours > 0;
    }
}
