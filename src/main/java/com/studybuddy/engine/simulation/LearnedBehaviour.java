package com.studybuddy.engine.simulation;

import java.util.Random;

/**
 * Behaviour taken straight from the app's request history: students send and answer requests
 * at the rates in BehaviourRates. Subclasses make students pickier or more eager by scaling
 * the chance of accepting.
 */
public class LearnedBehaviour implements BehaviourModel {

    private static final double NO_SCALING = 1.0;
    private static final double MAX_ACCEPT = 0.95;

    private final BehaviourRates rates;
    private final double acceptScale;

    /** Creates behaviour that follows the learned rates exactly. */
    public LearnedBehaviour(BehaviourRates rates) {
        this(rates, NO_SCALING);
    }

    /**
     * Creates behaviour whose chance of accepting is the learned one times acceptScale.
     *
     * @param rates the learned rates
     * @param acceptScale below 1 for pickier students, above 1 for more eager ones
     */
    protected LearnedBehaviour(BehaviourRates rates, double acceptScale) {
        this.rates = rates;
        this.acceptScale = acceptScale;
    }

    /** Returns "As students behave now". */
    @Override
    public String getLabel() {
        return "As students behave now";
    }

    /** Sends the whole part of the average, plus one more with a chance equal to the fraction. */
    @Override
    public int requestsToSend(Random random) {
        double average = rates.getRequestsPerRound().getValue();
        int whole = (int) average;
        double fraction = average - whole;
        if (random.nextDouble() < fraction) {
            return whole + 1;
        }
        return whole;
    }

    /** Ignores at the learned rate; otherwise accepts at the learned rate for the score band, lowered if busy. */
    @Override
    public Reply reply(double mutualScore, int inboxSize, Random random) {
        if (random.nextDouble() < rates.getIgnoreRate().getValue()) {
            return Reply.IGNORE;
        }
        if (random.nextDouble() < acceptChance(mutualScore, inboxSize)) {
            return Reply.ACCEPT;
        }
        return Reply.DECLINE;
    }

    /** Returns the chance of accepting an answered request, capped below 1. */
    private double acceptChance(double mutualScore, int inboxSize) {
        double chance = rates.getAcceptRate(mutualScore).getValue() * acceptScale;
        if (inboxSize >= BehaviourRates.BUSY_INBOX) {
            chance = chance * rates.getBusyFactor().getValue();
        }
        return Math.min(chance, MAX_ACCEPT);
    }
}
