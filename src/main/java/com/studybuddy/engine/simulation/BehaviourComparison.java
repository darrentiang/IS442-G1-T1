package com.studybuddy.engine.simulation;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.model.Student;

/**
 * The saved and proposed configs side by side under one behaviour, seen per student: who is at
 * risk of being left out (a low chance of ending with a buddy they can meet), who moved out of
 * risk and who is newly at risk. It reports what changes and does not judge it.
 */
public class BehaviourComparison {

    /** A student whose chance of a buddy they can meet is below this is at risk of being left out. */
    public static final double AT_RISK_CHANCE = 0.2;
    /** A student only counts as moved in or out of risk if their chance shifts by at least this, so luck alone does not move them. */
    public static final double MIN_SHIFT = 0.1;

    private final BehaviourModel behaviour;
    private final ConfigOutcome current;
    private final ConfigOutcome proposed;
    private final List<StudentChance> chances = new ArrayList<>();

    /**
     * Creates the comparison.
     *
     * @param behaviour the behaviour both configs were simulated under
     * @param current how the saved config did
     * @param proposed how the proposed config did
     * @param students the students with a profile, the ones who can be matched
     */
    public BehaviourComparison(BehaviourModel behaviour, ConfigOutcome current, ConfigOutcome proposed,
            List<Student> students) {
        this.behaviour = behaviour;
        this.current = current;
        this.proposed = proposed;
        for (Student student : students) {
            chances.add(new StudentChance(student, current.getChance(student.getId()),
                    proposed.getChance(student.getId())));
        }
    }

    /** Returns the behaviour both configs were simulated under. */
    public BehaviourModel getBehaviour() {
        return behaviour;
    }

    /** Returns how the saved config did. */
    public ConfigOutcome getCurrent() {
        return current;
    }

    /** Returns how the proposed config did. */
    public ConfigOutcome getProposed() {
        return proposed;
    }

    /** Returns how many students could be matched. */
    public int getStudentCount() {
        return chances.size();
    }

    /** Returns the change in the average chance of a buddy they can meet. */
    public Change getBuddyCoverageChange() {
        return Change.between(current.getBuddyCoverage(), proposed.getBuddyCoverage());
    }

    /** Returns how many students are at risk with the saved config. */
    public int getAtRiskSaved() {
        int count = 0;
        for (StudentChance chance : chances) {
            if (chance.getSavedChance() < AT_RISK_CHANCE) {
                count++;
            }
        }
        return count;
    }

    /** Returns how many students are at risk with the proposed config. */
    public int getAtRiskProposed() {
        int count = 0;
        for (StudentChance chance : chances) {
            if (chance.getProposedChance() < AT_RISK_CHANCE) {
                count++;
            }
        }
        return count;
    }

    /** Returns the students at risk with the saved config who are clearly out of risk with the proposed one. */
    public List<StudentChance> getMovedOutOfRisk() {
        List<StudentChance> moved = new ArrayList<>();
        for (StudentChance chance : chances) {
            if (chance.getSavedChance() < AT_RISK_CHANCE && chance.getProposedChance() >= AT_RISK_CHANCE
                    && chance.getProposedChance() - chance.getSavedChance() >= MIN_SHIFT) {
                moved.add(chance);
            }
        }
        return moved;
    }

    /** Returns the students not at risk with the saved config who are clearly at risk with the proposed one. */
    public List<StudentChance> getNewlyAtRisk() {
        List<StudentChance> moved = new ArrayList<>();
        for (StudentChance chance : chances) {
            if (chance.getSavedChance() >= AT_RISK_CHANCE && chance.getProposedChance() < AT_RISK_CHANCE
                    && chance.getSavedChance() - chance.getProposedChance() >= MIN_SHIFT) {
                moved.add(chance);
            }
        }
        return moved;
    }

    /** Returns the students at risk with both configs. */
    public List<StudentChance> getStillAtRisk() {
        List<StudentChance> still = new ArrayList<>();
        for (StudentChance chance : chances) {
            if (chance.getSavedChance() < AT_RISK_CHANCE && chance.getProposedChance() < AT_RISK_CHANCE) {
                still.add(chance);
            }
        }
        return still;
    }
}
