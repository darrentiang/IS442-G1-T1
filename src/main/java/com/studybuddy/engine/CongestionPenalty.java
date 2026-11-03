package com.studybuddy.engine;

/**
 * Gently lowers candidates who already have many pending requests. Without it the same strong
 * profile tops everyone's list and is flooded with requests they cannot all accept, while
 * other good matches are never contacted.
 */
public class CongestionPenalty {

    private static final double BASE = 1.0;

    /**
     * Applies the penalty: mutual / (1 + strength * pendingIncoming).
     *
     * @param mutual the mutual score before the adjustment
     * @param pendingIncoming how many PENDING requests the candidate has received
     * @param strength how strongly each pending request counts, e.g. 0.05
     * @return the adjusted score
     */
    public double apply(double mutual, int pendingIncoming, double strength) {
        return mutual / (BASE + strength * pendingIncoming);
    }
}
