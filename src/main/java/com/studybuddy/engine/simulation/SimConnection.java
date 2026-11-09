package com.studybuddy.engine.simulation;

/**
 * A pair of buddies in a simulation: how well they match, how much usable time they share, and
 * whether each one's buddy is relevant to the course they need help with.
 */
public class SimConnection {

    private final String firstId;
    private final String secondId;
    private final double mutualScore;
    private final int usableHours;
    private final boolean firstCourseRelevant;
    private final boolean secondCourseRelevant;
    private final boolean formedInSimulation;

    /**
     * Creates a connection.
     *
     * @param firstId one student's id
     * @param secondId the other student's id
     * @param mutualScore the pair's mutual match score
     * @param usableHours hours a week they are both free in blocks long enough for a session
     * @param firstCourseRelevant true if the second student is in or taking the first one's target course
     * @param secondCourseRelevant true if the first student is in or taking the second one's target course
     * @param formedInSimulation false for a connection that already exists in the app
     */
    public SimConnection(String firstId, String secondId, double mutualScore, int usableHours,
            boolean firstCourseRelevant, boolean secondCourseRelevant, boolean formedInSimulation) {
        this.firstId = firstId;
        this.secondId = secondId;
        this.mutualScore = mutualScore;
        this.usableHours = usableHours;
        this.firstCourseRelevant = firstCourseRelevant;
        this.secondCourseRelevant = secondCourseRelevant;
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

    /**
     * Returns true if this connection gives the student a buddy they can really study with: the
     * pair can meet, the buddy knows the student's target course, and the score is at least the minimum.
     * Only this kind counts as coverage.
     *
     * @param studentId the student the connection is judged for, either side
     * @param minimumScore the lowest mutual score that counts
     * @return true if the connection counts for that student
     */
    public boolean isUsableFor(String studentId, double minimumScore) {
        boolean courseRelevant = studentId.equals(firstId) ? firstCourseRelevant : secondCourseRelevant;
        return usableHours > 0 && courseRelevant && mutualScore >= minimumScore;
    }
}
