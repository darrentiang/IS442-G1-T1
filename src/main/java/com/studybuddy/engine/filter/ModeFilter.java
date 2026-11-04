package com.studybuddy.engine.filter;

import com.studybuddy.engine.CandidateFilter;
import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.StudyPreferences;

/** Applies the Mode search option: keeps candidates who can meet the way the seeker asked for. */
public class ModeFilter implements CandidateFilter {

    private static final String NAME = "mode";

    /** Returns "mode". */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Keeps everyone when no mode (or EITHER) is searched for; otherwise keeps candidates whose
     * mode is the one searched for or EITHER.
     */
    @Override
    public boolean accepts(Student seeker, Student candidate, MatchContext context, MatchingConfig config) {
        StudyMode wanted = context.getMode();
        if (wanted == null || wanted == StudyMode.EITHER) {
            return true;
        }
        StudentProfile profile = candidate.getProfile();
        if (profile == null || profile.getPreferences() == null) {
            return false;
        }
        StudyPreferences preferences = profile.getPreferences();
        return preferences.getMode() == wanted || preferences.getMode() == StudyMode.EITHER;
    }
}
