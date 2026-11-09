package com.studybuddy.engine.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.studybuddy.engine.MatchResult;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;

/**
 * Learns BehaviourRates from the app's buddy requests: how many each sender sent, how many were
 * left pending, and how often answered requests were accepted at each match score. Any number
 * based on fewer than MIN_OBSERVATIONS requests falls back to a default.
 */
public class BehaviourCalibrator {

    /** The fewest requests a number must be based on to be used instead of the default. */
    public static final int MIN_OBSERVATIONS = 10;

    private static final double DEFAULT_REQUESTS_PER_ROUND = 2.0;
    private static final double DEFAULT_IGNORE_RATE = 0.3;
    private static final double DEFAULT_LOW_ACCEPT = 0.2;
    private static final double DEFAULT_MEDIUM_ACCEPT = 0.4;
    private static final double DEFAULT_HIGH_ACCEPT = 0.6;
    private static final double DEFAULT_BUSY_FACTOR = 0.7;
    private static final double NO_EFFECT = 1.0;
    private static final int BANDS = 3;

    private final MatchingEngine engine;

    /** Creates a calibrator that scores past requests with the given engine. */
    public BehaviourCalibrator(MatchingEngine engine) {
        this.engine = engine;
    }

    /**
     * Learns the behaviour rates from the given requests.
     *
     * @param requests every buddy request in the app
     * @param config the settings used to score each past request
     * @return the learned rates, with defaults where data was too thin
     */
    public BehaviourRates calibrate(List<MatchRequest> requests, MatchingConfig config) {
        Set<String> senders = new HashSet<>();
        Map<String, Integer> received = new HashMap<>();
        int pending = 0;
        for (MatchRequest request : requests) {
            senders.add(request.getFrom().getId());
            String toId = request.getTo().getId();
            received.put(toId, countOf(received, toId) + 1);
            if (request.getStatus() == RequestStatus.PENDING) {
                pending++;
            }
        }

        int[] acceptedByBand = new int[BANDS];
        int[] answeredByBand = new int[BANDS];
        int busyAccepted = 0;
        int busyAnswered = 0;
        int quietAccepted = 0;
        int quietAnswered = 0;
        for (MatchRequest request : requests) {
            if (request.getStatus() == RequestStatus.PENDING) {
                continue;
            }
            MatchResult result = engine.evaluate(request.getFrom(), request.getTo(), config);
            if (result == null) {
                continue;
            }
            boolean accepted = request.getStatus() == RequestStatus.ACCEPTED;
            int band = BehaviourRates.bandOf(result.getMutualScore());
            answeredByBand[band]++;
            boolean busy = countOf(received, request.getTo().getId()) >= BehaviourRates.BUSY_INBOX;
            if (busy) {
                busyAnswered++;
            } else {
                quietAnswered++;
            }
            if (accepted) {
                acceptedByBand[band]++;
                if (busy) {
                    busyAccepted++;
                } else {
                    quietAccepted++;
                }
            }
        }

        return new BehaviourRates(
                requestsPerRound(requests.size(), senders.size()),
                Estimate.share(pending, requests.size(), DEFAULT_IGNORE_RATE, MIN_OBSERVATIONS),
                Estimate.share(acceptedByBand[0], answeredByBand[0], DEFAULT_LOW_ACCEPT, MIN_OBSERVATIONS),
                Estimate.share(acceptedByBand[1], answeredByBand[1], DEFAULT_MEDIUM_ACCEPT, MIN_OBSERVATIONS),
                Estimate.share(acceptedByBand[2], answeredByBand[2], DEFAULT_HIGH_ACCEPT, MIN_OBSERVATIONS),
                busyFactor(busyAccepted, busyAnswered, quietAccepted, quietAnswered));
    }

    /** Returns requests per sender; the request history is treated as one round of activity. */
    private Estimate requestsPerRound(int requestCount, int senderCount) {
        if (requestCount < MIN_OBSERVATIONS || senderCount == 0) {
            return new Estimate(DEFAULT_REQUESTS_PER_ROUND, requestCount, false);
        }
        return new Estimate((double) requestCount / senderCount, requestCount, true);
    }

    /** Returns busy acceptance divided by quiet acceptance, never above 1; the default if either side is thin. */
    private Estimate busyFactor(int busyAccepted, int busyAnswered, int quietAccepted, int quietAnswered) {
        int observations = busyAnswered + quietAnswered;
        if (busyAnswered < MIN_OBSERVATIONS || quietAnswered < MIN_OBSERVATIONS || quietAccepted == 0) {
            return new Estimate(DEFAULT_BUSY_FACTOR, observations, false);
        }
        double busyRate = (double) busyAccepted / busyAnswered;
        double quietRate = (double) quietAccepted / quietAnswered;
        return new Estimate(Math.min(busyRate / quietRate, NO_EFFECT), observations, true);
    }

    /** Returns the count stored for a student, or 0. */
    private int countOf(Map<String, Integer> counts, String studentId) {
        Integer count = counts.get(studentId);
        if (count == null) {
            return 0;
        }
        return count;
    }
}
