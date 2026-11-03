package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.model.Course;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyPreferences;

/** Applies the seeker's search options: a course they must be taking, a goal they must have. */
public class SearchFilter implements CandidateFilter {

    private static final String NAME = "search";

    /** Returns "search". */
    @Override
    public String getName() {
        return NAME;
    }

    /** Keeps the candidate only if they match every search option that is set. */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context) {
        StudentProfile profile = candidate.getProfile();
        if (profile == null) {
            return false;
        }
        if (context.getCourseCode() != null && !isTaking(profile, context.getCourseCode())) {
            return false;
        }
        if (context.getGoal() != null) {
            StudyPreferences preferences = profile.getPreferences();
            if (preferences == null || !preferences.getGoals().contains(context.getGoal())) {
                return false;
            }
        }
        return true;
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
