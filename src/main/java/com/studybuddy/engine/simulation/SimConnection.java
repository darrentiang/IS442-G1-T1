package com.studybuddy.engine.simulation;

/** A pair of buddies in a simulation, with how well they match and how much usable time they share. */
public class SimConnection {

    private final String firstId;
    private final String secondId;
    private final double mutualScore;
    private final int usableHours;
    private final boolean formedInSimulation;

    /**
     * Creates a connection.
     *
     * @param firstId one student's id
     * @param secondId the other student's id
     * @param mutualScore the pair's mutual match score
     * @param usableHours hours a week they are both free in blocks long enough for a session
     * @param formedInSimulation false for a connection that already exists in the app
     */
    public SimConnection(String firstId, String secondId, double mutualScore, int usableHours,
            boolean formedInSimulation) {
        this.firstId = firstId;
        this.secondId = secondId;
        this.mutualScore = mutualScore;
        this.usableHours = usableHours;
        this.formedInSimulation = formedInSimulation;
    }

    /** Returns one student's id. */
    public String getFirstId() {
        return firstId;
    }

    /** Returns the other student's id. */
    public String getSecondId() {
        return secondId;
    }

    /** Returns the pair's mutual match score. */
    public double getMutualScore() {
        return mutualScore;
    }

    /** Returns the hours a week they share in blocks long enough for a session. */
    public int getUsableHours() {
        return usableHours;
    }

    /** Returns true if the connection was made during the simulation rather than already existing. */
    public boolean isFormedInSimulation() {
        return formedInSimulation;
    }

    /** Returns true if the pair can actually meet and scores at least the minimum: the only kind that counts as coverage. */
    public boolean isUsable(double minimumScore) {
        return usableHours > 0 && mutualScore >= minimumScore;
    }
}
