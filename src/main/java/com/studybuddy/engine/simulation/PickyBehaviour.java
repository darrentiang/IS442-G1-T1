package com.studybuddy.engine.simulation;

/** Learned behaviour, but students accept 30% less often: tests that a result holds if students get choosier. */
public class PickyBehaviour extends LearnedBehaviour {

    private static final double ACCEPT_SCALE = 0.7;

    /** Creates picky behaviour from the learned rates. */
    public PickyBehaviour(BehaviourRates rates) {
        super(rates, ACCEPT_SCALE);
    }

    /** Returns "If students accept 30% less often". */
    @Override
    public String getLabel() {
        return "If students accept 30% less often";
    }
}
