package com.studybuddy.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyMode;

/**
 * The details of one search that the engine needs but cannot look up itself: the search
 * options, who to leave out, and how many requests each candidate is already waiting on.
 * The service builds it; the engine only reads it.
 */
public class MatchContext {

    private static final int NO_REQUESTS = 0;

    private final String courseCode;
    private final StudyGoal goal;
    private final StudyMode mode;
    private final int minUsableHours;
    private final Set<String> excludedIds;
    private final Map<String, Integer> pendingIncoming;

    /**
     * Creates a context for one search.
     *
     * @param courseCode only show candidates taking this course, or null for any course
     * @param goal only show candidates with this goal, or null for any goal
     * @param mode only show candidates who can meet this way, or null (or EITHER) for any mode
     * @param minUsableHours only show candidates with at least this many usable hours together; 0 for no limit
     * @param excludedIds ids of students to leave out; null means none
     * @param pendingIncoming candidate id to the number of PENDING requests they have received; null means none
     */
    public MatchContext(String courseCode, StudyGoal goal, StudyMode mode, int minUsableHours,
            Set<String> excludedIds, Map<String, Integer> pendingIncoming) {
        this.courseCode = courseCode;
        this.goal = goal;
        this.mode = mode;
        this.minUsableHours = minUsableHours;
        Set<String> excludedCopy = new HashSet<>();
        if (excludedIds != null) {
            excludedCopy.addAll(excludedIds);
        }
        this.excludedIds = Collections.unmodifiableSet(excludedCopy);
        Map<String, Integer> pendingCopy = new HashMap<>();
        if (pendingIncoming != null) {
            pendingCopy.putAll(pendingIncoming);
        }
        this.pendingIncoming = Collections.unmodifiableMap(pendingCopy);
    }

    /** Returns the course code searched for, or null for any course. */
    public String getCourseCode() {
        return courseCode;
    }

    /** Returns the goal searched for, or null for any goal. */
    public StudyGoal getGoal() {
        return goal;
    }

    /** Returns the study mode searched for, or null for any mode. */
    public StudyMode getMode() {
        return mode;
    }

    /** Returns the fewest usable hours together a candidate must have; 0 means no limit. */
    public int getMinUsableHours() {
        return minUsableHours;
    }

    /** Returns the ids of students to leave out (read-only). */
    public Set<String> getExcludedIds() {
        return excludedIds;
    }

    /** Returns candidate id to number of PENDING requests received (read-only). */
    public Map<String, Integer> getPendingIncoming() {
        return pendingIncoming;
    }

    /** Returns how many PENDING requests the given student has received, or 0 if none. */
    public int getPendingIncoming(String studentId) {
        Integer count = pendingIncoming.get(studentId);
        if (count == null) {
            return NO_REQUESTS;
        }
        return count;
    }
}
