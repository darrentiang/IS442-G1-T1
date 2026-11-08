package com.studybuddy.engine.simulation;

/** Learned behaviour, but students accept 30% more often: tests that a result holds if students get keener. */
public class EagerBehaviour extends LearnedBehaviour {

    private static final double ACCEPT_SCALE = 1.3;

    /** Creates eager behaviour from the learned rates. */
    public EagerBehaviour(BehaviourRates rates) {
        super(rates, ACCEPT_SCALE);
    }

    /** Returns "eager". */
    @Override
    public String getName() {
        return "eager";
    }

    /** Returns "More eager students". */
    @Override
    public String getLabel() {
        return "More eager students";
    }
}
