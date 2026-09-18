package com.studybuddy.engine;

import java.util.List;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;

/** One way of combining criterion scores into a ranking. */
public interface MatchingStrategy {

    /** Returns the strategy name, e.g. "balanced" — used as the config key. */
    String getName();

    /** Ranks the candidates for the seeker, best first, honouring the given config. */
    List<MatchResult> rank(StudentProfile seeker, List<Student> candidates, MatchingConfig config);
}
