package com.studybuddy.engine.simulation;

/**
 * One number the simulator learned from the app's request history, with how many requests it
 * was based on. When there were too few, the default value is used instead and isLearned() says so.
 */
public class Estimate {

    private final double value;
    private final int observations;
    private final boolean learned;

    /**
     * Creates an estimate.
     *
     * @param value the number to use
     * @param observations how many requests it was worked out from
     * @param learned true if it came from the data, false if it is the default
     */
    public Estimate(double value, int observations, boolean learned) {
        this.value = value;
        this.observations = observations;
        this.learned = learned;
    }

    /**
     * Returns successes / total, or the default when total is below the minimum.
     *
     * @param successes how many times it happened
     * @param total how many times it could have happened
     * @param defaultValue the value to use when there is too little data
     * @param minObservations the fewest observations that count as enough
     * @return the estimate
     */
    public static Estimate share(int successes, int total, double defaultValue, int minObservations) {
        if (total < minObservations) {
            return new Estimate(defaultValue, total, false);
        }
        return new Estimate((double) successes / total, total, true);
    }

    /** Returns the number to use. */
    public double getValue() {
        return value;
    }

    /** Returns how many requests the number was worked out from. */
    public int getObservations() {
        return observations;
    }

    /** Returns true if the number came from the data, false if it is the default. */
    public boolean isLearned() {
        return learned;
    }
}
