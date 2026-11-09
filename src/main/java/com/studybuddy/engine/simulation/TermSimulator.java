package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchResult;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;
import com.studybuddy.model.Connection;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;

/**
 * Plays out one simulated term for one matching config. It starts from the app's current
 * connections and pending requests, then runs rounds: every student still without a buddy
 * asks the real engine for their list and sends requests to the top of it, then everyone
 * answers their inbox. Works only on copies; the real students and requests are never changed.
 *
 * The config being tested only decides who appears on each student's list. How good a pair
 * really is (the score students react to, and whether they can meet) is always judged with one fixed yardstick config, so a config cannot look better by lowering its own bar.
 */
public class TermSimulator {

    /** A request left unanswered for this many rounds is dropped, as if the sender gave up. */
    public static final int EXPIRY_ROUNDS = 2;

    private static final double NO_SCORE = 0.0;
    private static final int NO_HOURS = 0;

    private final MatchingEngine engine;
    private final MatchingConfig config;
    private final MatchingConfig yardstick;
    private final BehaviourModel behaviour;
    private final int rounds;
    private final Map<String, MatchResult> pairCache = new HashMap<>();

    /**
     * Creates a simulator for one config and one behaviour. Pair scores are remembered between
     * terms, since they only depend on the two profiles and the config.
     *
     * @param engine the real matching engine
     * @param config the settings being tested
     * @param yardstick the settings every pair is judged by, the same for every config compared
     * @param behaviour how simulated students send and answer requests
     * @param rounds how many rounds of requests to play out
     */
    public TermSimulator(MatchingEngine engine, MatchingConfig config, MatchingConfig yardstick,
            BehaviourModel behaviour, int rounds) {
        this.engine = engine;
        this.config = config;
        this.yardstick = yardstick;
        this.behaviour = behaviour;
        this.rounds = rounds;
    }

    /**
     * Runs one term.
     *
     * @param students every student in the app
     * @param requests every buddy request in the app; only PENDING ones are carried in
     * @param connections every connection in the app; only active ones are carried in
     * @param seed the random seed, so the same seed always gives the same term
     * @return the numbers from this term
     */
    public TermResult run(List<Student> students, List<MatchRequest> requests, List<Connection> connections,
            long seed) {
        Random random = new Random(seed);
        Map<String, SimStudent> byId = new LinkedHashMap<>();
        for (Student student : students) {
            byId.put(student.getId(), new SimStudent(student));
        }
        loadConnections(byId, connections);
        loadPendingRequests(byId, requests);

        for (int round = 0; round < rounds; round++) {
            sendRequests(byId, students, random);
            answerRequests(byId, random);
        }
        return buildResult(byId);
    }

    /** Carries the app's active connections into the simulation. */
    private void loadConnections(Map<String, SimStudent> byId, List<Connection> connections) {
        for (Connection connection : connections) {
            SimStudent first = byId.get(connection.getA().getId());
            SimStudent second = byId.get(connection.getB().getId());
            if (connection.isActive() && first != null && second != null) {
                connect(first, second);
            }
        }
    }

    /** Carries the app's pending requests into the simulated inboxes. */
    private void loadPendingRequests(Map<String, SimStudent> byId, List<MatchRequest> requests) {
        for (MatchRequest request : requests) {
            SimStudent from = byId.get(request.getFrom().getId());
            SimStudent to = byId.get(request.getTo().getId());
            if (request.getStatus() == RequestStatus.PENDING && from != null && to != null) {
                to.receive(new SimRequest(from.getId(), pairScore(from, to)));
                from.markContacted(to.getId());
            }
        }
    }

    /**
     * Send step: each student without a buddy gets their list from the real engine, using the
     * inbox sizes at the start of the round for congestion, and sends requests to the top of it.
     */
    private void sendRequests(Map<String, SimStudent> byId, List<Student> students, Random random) {
        Map<String, Integer> pendingCounts = new HashMap<>();
        for (SimStudent student : byId.values()) {
            pendingCounts.put(student.getId(), student.getInboxSize());
        }
        List<SimStudent> order = new ArrayList<>(byId.values());
        Collections.shuffle(order, random);
        for (SimStudent sender : order) {
            if (!sender.isSearching()) {
                continue;
            }
            MatchContext context = new MatchContext(null, null, null, 0, sender.getContactedIds(), pendingCounts);
            List<MatchResult> list = engine.findMatches(sender.getStudent(), students, context, config);
            int toSend = Math.min(behaviour.requestsToSend(random), list.size());
            for (int i = 0; i < toSend; i++) {
                SimStudent receiver = byId.get(list.get(i).getCandidate().getId());
                if (receiver != null) {
                    receiver.receive(new SimRequest(sender.getId(), pairScore(sender, receiver)));
                    sender.markContacted(receiver.getId());
                }
            }
        }
    }

    /** Answer step: each student answers every request in their inbox; unanswered ones expire after EXPIRY_ROUNDS. */
    private void answerRequests(Map<String, SimStudent> byId, Random random) {
        List<SimStudent> order = new ArrayList<>(byId.values());
        Collections.shuffle(order, random);
        for (SimStudent receiver : order) {
            List<SimRequest> waiting = new ArrayList<>(receiver.getInbox());
            for (SimRequest request : waiting) {
                Reply reply = behaviour.reply(request.getMutualScore(), receiver.getInboxSize(), random);
                if (reply == Reply.ACCEPT) {
                    receiver.removeFromInbox(request);
                    connect(byId.get(request.getFromId()), receiver);
                } else if (reply == Reply.DECLINE) {
                    receiver.removeFromInbox(request);
                } else {
                    request.waitOneRound();
                    if (request.getRoundsWaiting() >= EXPIRY_ROUNDS) {
                        receiver.removeFromInbox(request);
                    }
                }
            }
        }
    }

    /** Connects two simulated students; whether they can meet is judged with the yardstick. */
    private void connect(SimStudent first, SimStudent second) {
        MatchResult result = sideResult(first, second);
        int hours = NO_HOURS;
        if (result != null) {
            hours = result.getUsableHours();
        }
        SimConnection connection = new SimConnection(hours);
        first.addConnection(connection, second.getId());
        second.addConnection(connection, first.getId());
    }

    /** Returns the pair's yardstick mutual score, or 0 if either has no profile. Mutual scores are the same both ways. */
    private double pairScore(SimStudent first, SimStudent second) {
        MatchResult result = sideResult(first, second);
        if (result == null) {
            return NO_SCORE;
        }
        return result.getMutualScore();
    }

    /** Returns the yardstick result from the seeker's side, worked out once and then remembered. */
    private MatchResult sideResult(SimStudent seeker, SimStudent candidate) {
        String key = seeker.getId() + ">" + candidate.getId();
        if (pairCache.containsKey(key)) {
            return pairCache.get(key);
        }
        MatchResult result = engine.evaluate(seeker.getStudent(), candidate.getStudent(), yardstick);
        pairCache.put(key, result);
        return result;
    }

    /** Works out which students ended with a buddy they can meet, and what share of students that is. */
    private TermResult buildResult(Map<String, SimStudent> byId) {
        int withProfile = 0;
        Set<String> withBuddy = new HashSet<>();
        for (SimStudent student : byId.values()) {
            if (student.getStudent().getProfile() == null) {
                continue;
            }
            withProfile++;
            if (student.hasBuddyToMeet()) {
                withBuddy.add(student.getId());
            }
        }
        double buddyCoverage = 0;
        if (withProfile > 0) {
            buddyCoverage = (double) withBuddy.size() / withProfile;
        }
        return new TermResult(buddyCoverage, withBuddy);
    }
}
