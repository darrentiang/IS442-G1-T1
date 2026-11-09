package com.studybuddy.engine.simulation;

import java.util.Random;

/**
 * How simulated students behave: how many requests they send each round and how they answer
 * the requests they receive. Implementations can be swapped to test whether a result still
 * holds when students are pickier or more eager.
 */
public interface BehaviourModel {

    /** Returns the name shown to admins, e.g. "As students behave now". */
    String getLabel();

    /**
     * Decides how many requests a searching student sends this round.
     *
     * @param random the run's random number source
     * @return the number of requests, zero or more
     */
    int requestsToSend(Random random);

    /**
     * Decides how a student answers one request.
     *
     * @param mutualScore the pair's mutual match score, 0 to 1
     * @param inboxSize how many requests the student currently has waiting
     * @param random the run's random number source
     * @return accept, decline or ignore
     */
    Reply reply(double mutualScore, int inboxSize, Random random);
}
