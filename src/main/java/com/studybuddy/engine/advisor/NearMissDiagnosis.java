package com.studybuddy.engine.advisor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.criteria.AvailabilityCriterion;
import com.studybuddy.engine.criteria.GroupSizeCriterion;
import com.studybuddy.engine.criteria.StudyGoalCriterion;
import com.studybuddy.engine.criteria.StudyModeCriterion;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;

/**
 * Works out why people almost matched the student. For each near miss it adds up the points lost on
 * each preference she can change, from both sides of the pair (weight x (1 - score), hers plus
 * theirs), and takes the biggest as that person's blocker. It then counts the blockers and turns
 * each one into an exact change, best first. The numbers are an estimate: the advisor checks every
 * change with the real engine before showing it.
 */
public class NearMissDiagnosis {

    /** The preferences a student can change, in the order ties are broken. */
    private static final String[] CHANGEABLE = {
        StudyModeCriterion.NAME, AvailabilityCriterion.NAME, StudyGoalCriterion.NAME, GroupSizeCriterion.NAME
    };
    private static final double FULL_SCORE = 1.0;
    private static final double NO_LOSS = 0.0;

    /**
     * Diagnoses the near misses.
     *
     * @param her the advised student's profile
     * @param nearMisses the people who scored just under good
     * @param weights criterion name to weight, from the matching settings
     * @param sessionHours the length of a time block to suggest
     * @return one blocker per preference that held someone back, most people first
     */
    public List<Blocker> diagnose(StudentProfile her, List<NearMiss> nearMisses, Map<String, Double> weights,
            int sessionHours) {
        List<List<NearMiss>> heldBack = new ArrayList<>();
        for (int i = 0; i < CHANGEABLE.length; i++) {
            heldBack.add(new ArrayList<>());
        }
        for (NearMiss nearMiss : nearMisses) {
            int blocker = mainBlocker(nearMiss, weights);
            if (blocker >= 0) {
                heldBack.get(blocker).add(nearMiss);
            }
        }

        List<Blocker> blockers = new ArrayList<>();
        for (int i = 0; i < CHANGEABLE.length; i++) {
            List<NearMiss> people = heldBack.get(i);
            if (people.isEmpty()) {
                continue;
            }
            PreferenceChange change = changeFor(CHANGEABLE[i], her, people, sessionHours);
            if (change != null && change.appliesTo(her)) {
                insertByPeople(blockers, new Blocker(change, people.size()));
            }
        }
        return blockers;
    }

    /** Returns the index in CHANGEABLE of the preference that cost this pair the most points, or -1 if none did. */
    private int mainBlocker(NearMiss nearMiss, Map<String, Double> weights) {
        int best = -1;
        double bestLoss = NO_LOSS;
        for (int i = 0; i < CHANGEABLE.length; i++) {
            double weight = weightOf(CHANGEABLE[i], weights);
            double loss = weight * lossOf(nearMiss.getHerSide().get(CHANGEABLE[i]))
                    + weight * lossOf(nearMiss.getTheirSide().get(CHANGEABLE[i]));
            if (loss > bestLoss) {
                bestLoss = loss;
                best = i;
            }
        }
        return best;
    }

    /** Returns the exact change that relaxes the given preference for these people, or null if there is none. */
    private PreferenceChange changeFor(String criterion, StudentProfile her, List<NearMiss> people, int sessionHours) {
        if (StudyModeCriterion.NAME.equals(criterion)) {
            return new WidenModeChange(her.getPreferences().getMode());
        }
        if (GroupSizeCriterion.NAME.equals(criterion)) {
            return new WidenFormatChange(her.getPreferences().getFormat());
        }
        if (StudyGoalCriterion.NAME.equals(criterion)) {
            StudyGoal goal = mostWantedGoal(her, people);
            return goal == null ? null : new AddGoalChange(goal);
        }
        return bestTimeBlock(her, people, sessionHours);
    }

    /** Returns the goal most of these people have that the student does not, or null. */
    private StudyGoal mostWantedGoal(StudentProfile her, List<NearMiss> people) {
        Map<StudyGoal, Integer> counts = new EnumMap<>(StudyGoal.class);
        for (NearMiss nearMiss : people) {
            StudentProfile theirs = nearMiss.getCandidate().getProfile();
            if (theirs == null || theirs.getPreferences() == null) {
                continue;
            }
            for (StudyGoal goal : theirs.getPreferences().getGoals()) {
                if (!her.getPreferences().getGoals().contains(goal)) {
                    Integer count = counts.get(goal);
                    counts.put(goal, count == null ? 1 : count + 1);
                }
            }
        }
        StudyGoal best = null;
        int bestCount = 0;
        for (Map.Entry<StudyGoal, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    /** Returns the block of sessionHours when most of these people are free and the student is not yet, or null. */
    private AddTimeSlotChange bestTimeBlock(StudentProfile her, List<NearMiss> people, int sessionHours) {
        AddTimeSlotChange best = null;
        int bestCount = 0;
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            for (int start = 0; start + sessionHours <= AvailabilityGrid.HOURS_PER_DAY; start++) {
                AddTimeSlotChange block = new AddTimeSlotChange(day, start, sessionHours);
                if (!block.appliesTo(her)) {
                    continue;
                }
                int free = countFree(people, day, start, sessionHours);
                if (free > bestCount) {
                    bestCount = free;
                    best = block;
                }
            }
        }
        return best;
    }

    /** Returns how many of these people are free for the whole block. */
    private int countFree(List<NearMiss> people, int day, int start, int hours) {
        int count = 0;
        for (NearMiss nearMiss : people) {
            StudentProfile theirs = nearMiss.getCandidate().getProfile();
            if (theirs == null || theirs.getAvailability() == null) {
                continue;
            }
            boolean freeThroughout = true;
            for (int hour = start; hour < start + hours; hour++) {
                if (!theirs.getAvailability().isAvailable(day, hour)) {
                    freeThroughout = false;
                    break;
                }
            }
            if (freeThroughout) {
                count++;
            }
        }
        return count;
    }

    /** Adds the blocker so the list stays sorted by people held back, most first. */
    private void insertByPeople(List<Blocker> blockers, Blocker blocker) {
        int index = 0;
        while (index < blockers.size() && blockers.get(index).getPeople() >= blocker.getPeople()) {
            index++;
        }
        blockers.add(index, blocker);
    }

    /** Returns the points a criterion score falls short of full marks; 0 if the criterion was not scored. */
    private double lossOf(CriterionScore score) {
        if (score == null) {
            return NO_LOSS;
        }
        return FULL_SCORE - score.getValue();
    }

    /** Returns the configured weight for a criterion, or 0 when it has none. */
    private double weightOf(String criterion, Map<String, Double> weights) {
        if (weights == null) {
            return NO_LOSS;
        }
        Double weight = weights.get(criterion);
        return weight == null ? NO_LOSS : weight;
    }
}
