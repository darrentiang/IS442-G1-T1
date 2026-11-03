package com.studybuddy.engine.criteria;

import java.util.List;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.Course;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyPreferences;

/** Scores whether the candidate can help with the course the seeker needs a buddy for. */
public class CourseCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "course";
    private static final String LABEL = "Course";

    private static final double SAME_TARGET = 1.0;
    private static final double TAKING_TARGET = 0.6;
    private static final double SHARE_ANY_COURSE = 0.3;
    private static final double NO_FIT = 0.0;

    /** Returns "course". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Course". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /**
     * 1.0 if both target the same course, 0.6 if the candidate currently takes the seeker's
     * target course, 0.3 if they share any current course, else 0.0.
     */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        StudyPreferences seekerPreferences = seeker.getPreferences();
        if (seekerPreferences == null || seekerPreferences.getTargetCourse() == null) {
            return new CriterionScore(NO_FIT, null);
        }
        String targetCode = seekerPreferences.getTargetCourse().getCode();
        StudyPreferences candidatePreferences = candidate.getPreferences();
        if (candidatePreferences != null && candidatePreferences.getTargetCourse() != null
                && candidatePreferences.getTargetCourse().getCode().equals(targetCode)) {
            return new CriterionScore(SAME_TARGET, "Both looking for " + targetCode + " partners");
        }
        if (isTaking(candidate.getCoursesTaken(), targetCode)) {
            return new CriterionScore(TAKING_TARGET, "Also taking " + targetCode);
        }
        for (Course course : seeker.getCoursesTaken()) {
            if (isTaking(candidate.getCoursesTaken(), course.getCode())) {
                return new CriterionScore(SHARE_ANY_COURSE, null);
            }
        }
        return new CriterionScore(NO_FIT, null);
    }

    /** Returns true if the list holds a course with the given code. */
    private boolean isTaking(List<Course> courses, String courseCode) {
        for (Course course : courses) {
            if (course.getCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }
}
