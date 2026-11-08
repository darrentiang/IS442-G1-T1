package com.studybuddy.engine.simulation;

/**
 * The current and proposed configs side by side under one behaviour, with the three checks:
 * coverage clearly higher, match quality within the guardrail, and inboxes no more crowded.
 */
public class BehaviourComparison {

    /** The proposed config may lower the average match score of new connections by at most this much. */
    public static final double QUALITY_MARGIN = 0.05;
    /** The proposed config may raise the average peak inbox by at most this many requests. */
    public static final double INBOX_MARGIN = 1.0;

    private final BehaviourModel behaviour;
    private final ConfigOutcome current;
    private final ConfigOutcome proposed;

    /**
     * Creates the comparison.
     *
     * @param behaviour the behaviour both configs were simulated under
     * @param current how the current config did
     * @param proposed how the proposed config did
     */
    public BehaviourComparison(BehaviourModel behaviour, ConfigOutcome current, ConfigOutcome proposed) {
        this.behaviour = behaviour;
        this.current = current;
        this.proposed = proposed;
    }

    /** Returns the behaviour both configs were simulated under. */
    public BehaviourModel getBehaviour() {
        return behaviour;
    }

    /** Returns how the current config did. */
    public ConfigOutcome getCurrent() {
        return current;
    }

    /** Returns how the proposed config did. */
    public ConfigOutcome getProposed() {
        return proposed;
    }

    /** Returns true if the proposed config's coverage interval sits entirely above the current one's. */
    public boolean isCoverageClearlyHigher() {
        return proposed.getCoverage().isClearlyAbove(current.getCoverage());
    }

    /** Returns true if the proposed config's coverage interval sits entirely below the current one's. */
    public boolean isCoverageClearlyLower() {
        return current.getCoverage().isClearlyAbove(proposed.getCoverage());
    }

    /** Returns true if new connections score no more than QUALITY_MARGIN below the current config's. */
    public boolean isQualityKept() {
        return proposed.getAverageScore().getMean() >= current.getAverageScore().getMean() - QUALITY_MARGIN;
    }

    /** Returns true if the peak inbox grows by no more than INBOX_MARGIN. */
    public boolean isInboxKept() {
        return proposed.getPeakInbox().getMean() <= current.getPeakInbox().getMean() + INBOX_MARGIN;
    }

    /** Returns true if all three checks pass. */
    public boolean isProposedBetter() {
        return isCoverageClearlyHigher() && isQualityKept() && isInboxKept();
    }
}
