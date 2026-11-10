package com.studybuddy.engine.advisor;

import java.util.EnumSet;
import java.util.Set;

import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;

/** Adds one study goal to the student's goals. */
public class AddGoalChange extends PreferenceChange {

    private final StudyGoal goal;

    /** Creates the change that adds the given goal. */
    public AddGoalChange(StudyGoal goal) {
        this.goal = goal;
    }

    /** Returns e.g. "goal:EXAM_PREP". */
    @Override
    public String getKey() {
        return "goal:" + goal.name();
    }

    /** Applies unless the student already has this goal. */
    @Override
    public boolean appliesTo(StudentProfile profile) {
        return profile.getPreferences() != null && !profile.getPreferences().getGoals().contains(goal);
    }

    /** Adds the goal, keeping the student's other goals. */
    @Override
    public void applyTo(StudentProfile profile) {
        Set<StudyGoal> goals = EnumSet.noneOf(StudyGoal.class);
        goals.addAll(profile.getPreferences().getGoals());
        goals.add(goal);
        profile.getPreferences().setGoals(goals);
    }

    /** Returns e.g. "Add Exam prep to your study goals". */
    @Override
    public String describe() {
        return "Add " + goal.getLabel() + " to your study goals";
    }

    /** Returns e.g. "Added Exam prep to your study goals". */
    @Override
    public String getDoneMessage() {
        return "Added " + goal.getLabel() + " to your study goals";
    }

    /** Returns that some sessions may now cover this goal. */
    @Override
    public String getTradeOff() {
        return "Some of your sessions may cover " + goal.getLabel().toLowerCase() + " as well.";
    }

    /** Returns "study goal". */
    @Override
    public String getBlockerLabel() {
        return "study goal";
    }
}
