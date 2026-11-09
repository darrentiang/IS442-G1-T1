package com.studybuddy.engine.simulation;

/**
 * Collects one number from many simulated terms and gives its average with a 95% confidence
 * interval: the range the true average very likely falls in, given the luck of the draw.
 */
public class Statistic {

    /** Multiplier for a 95% confidence interval. */
    private static final double Z_95 = 1.96;

    private int count;
    private double sum;
    private double sumOfSquares;

    /** Adds one term's value. */
    public void add(double value) {
        count++;
        sum += value;
        sumOfSquares += value * value;
    }

    /** Returns the average, or 0 if nothing was added. */
    public double getMean() {
        if (count == 0) {
            return 0;
        }
        return sum / count;
    }

    /** Returns the half-width of the 95% confidence interval: the "± x" part. */
    public double getMargin() {
        if (count < 2) {
            return 0;
        }
        double mean = getMean();
        double variance = (sumOfSquares - count * mean * mean) / (count - 1);
        if (variance < 0) {
            variance = 0;
        }
        return Z_95 * Math.sqrt(variance / count);
    }

    /** Returns the bottom of the 95% confidence interval. */
    private double getLower() {
        return getMean() - getMargin();
    }

    /** Returns the top of the 95% confidence interval. */
    private double getUpper() {
        return getMean() + getMargin();
    }

    /** Returns true if this interval sits entirely above the other one, so the difference is not luck. */
    public boolean isClearlyAbove(Statistic other) {
        return getLower() > other.getUpper();
    }
}
