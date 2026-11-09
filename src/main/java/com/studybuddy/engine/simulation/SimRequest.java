package com.studybuddy.engine.simulation;

/** One buddy request waiting in a simulated student's inbox, with its match score and how many rounds it has waited. */
public class SimRequest {

    private final String fromId;
    private final double mutualScore;
    private int roundsWaiting;

    /**
     * Creates a request that has not waited yet.
     *
     * @param fromId the id of the student who sent it
     * @param mutualScore the pair's mutual match score
     */
    public SimRequest(String fromId, double mutualScore) {
        this.fromId = fromId;
        this.mutualScore = mutualScore;
    }

    /** Returns the id of the student who sent the request. */
    public String getFromId() {
        return fromId;
    }

    /** Returns the pair's mutual match score. */
    public double getMutualScore() {
        return mutualScore;
    }

    /** Returns how many rounds the request has been left unanswered. */
    public int getRoundsWaiting() {
        return roundsWaiting;
    }

    /** Records that the request was left unanswered for one more round. */
    public void waitOneRound() {
        roundsWaiting++;
    }
}
