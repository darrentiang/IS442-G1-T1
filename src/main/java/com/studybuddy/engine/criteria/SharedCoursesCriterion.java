package com.studybuddy.engine.criteria;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchCriterion;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.Course;
import com.studybuddy.model.StudentProfile;

/** Scores the other current courses the pair have in common, besides the seeker's target course. */
public class SharedCoursesCriterion implements MatchCriterion {

    /** The config key for this criterion. */
    public static final String NAME = "sharedCourses";
    private static final String LABEL = "Shared courses";

    private static final double FULL_SCORE = 1.0;
    private static final double COURSES_FOR_FULL_SCORE = 2.0;
    private static final double NO_FIT = 0.0;
    private static final double REASON_THRESHOLD = 0.6;

    /** Returns "sharedCourses". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Returns "Shared courses". */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /** min(1.0, n / 2) where n is the current courses in common other than the seeker's target. */
    @Override
    public CriterionScore score(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        String targetCode = null;
        if (seeker.getPreferences() != null && seeker.getPreferences().getTargetCourse() != null) {
            targetCode = seeker.getPreferences().getTargetCourse().getCode();
        }
        int shared = 0;
        StringBuilder sharedText = new StringBuilder();
        for (Course course : seeker.getCoursesTaken()) {
            String code = course.getCode();
            if (code.equals(targetCode) || !isTaking(candidate, code)) {
                continue;
            }
            if (shared > 0) {
                sharedText.append(", ");
            }
            sharedText.append(code);
            shared++;
        }
        double value = Math.min(FULL_SCORE, shared / COURSES_FOR_FULL_SCORE);
        String reason = null;
        if (value >= REASON_THRESHOLD) {
            reason = "Also both taking " + sharedText;
        }
        return new CriterionScore(value, reason);
    }

    /** Returns true if the profile lists the course among its current courses. */
    private boolean isTaking(StudentProfile profile, String courseCode) {
        for (Course course : profile.getCoursesTaken()) {
            if (course.getCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }
}
