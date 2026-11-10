package com.studybuddy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.studybuddy.engine.CriterionScore;
import com.studybuddy.engine.MatchResult;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.advisor.Advice;
import com.studybuddy.engine.advisor.Blocker;
import com.studybuddy.engine.advisor.NearMiss;
import com.studybuddy.engine.advisor.NearMissDiagnosis;
import com.studybuddy.engine.advisor.PreferenceChange;
import com.studybuddy.engine.advisor.Suggestion;
import com.studybuddy.engine.advisor.SuggestionComparator;
import com.studybuddy.engine.advisor.WhatIfCopier;
import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.Course;
import com.studybuddy.model.Student;
import com.studybuddy.repository.StudentRepository;

/**
 * BOSS Advisor: helps a student with too few good matches. It looks at the people who almost
 * matched them, works out which preference held most of them back (from both sides of each pair),
 * tries the matching change on a copy of the student, and keeps it only if the real Find buddies
 * search confirms it adds good matches. The real profile changes only when the student applies one.
 */
@Service
public class BossAdvisorService {

    private final MatchingService matchingService;
    private final StudentRepository studentRepository;
    private final WhatIfCopier copier = new WhatIfCopier();
    private final NearMissDiagnosis diagnosis = new NearMissDiagnosis();
    private final double goodScore;
    private final double nearMissScore;
    private final int enoughGoodMatches;
    private final int maxSuggestions;

    /** Creates the service with what it uses and its thresholds from application.properties (constructor injection). */
    public BossAdvisorService(MatchingService matchingService, StudentRepository studentRepository,
            Environment environment) {
        this.matchingService = matchingService;
        this.studentRepository = studentRepository;
        this.goodScore = environment.getRequiredProperty("studybuddy.advisor.good-match-score", Double.class);
        this.nearMissScore = environment.getRequiredProperty("studybuddy.advisor.near-miss-score", Double.class);
        this.enoughGoodMatches = environment.getRequiredProperty("studybuddy.advisor.enough-good-matches", Integer.class);
        this.maxSuggestions = environment.getRequiredProperty("studybuddy.advisor.max-suggestions", Integer.class);
    }

    /**
     * Works out the advice for a student, on their own target course.
     *
     * @param student the signed-in student
     * @return the advice, or null if they have no profile or no target course yet
     */
    public Advice adviceFor(Student student) {
        Course target = targetCourse(student);
        if (target == null) {
            return null;
        }
        BuddySearch search = targetSearch(target);
        List<MatchResult> results = matchingService.findBuddies(student, search);
        int goodBefore = countGood(results);
        if (goodBefore >= enoughGoodMatches) {
            return Advice.notNeeded(goodBefore);
        }
        double bestBefore = bestScore(results);

        MatchingConfig config = matchingService.getConfig();
        List<Blocker> blockers = diagnosis.diagnose(student.getProfile(), nearMisses(student, results),
                config.getWeights(), config.getMinSessionHours());

        List<Suggestion> suggestions = new ArrayList<>();
        for (Blocker blocker : blockers) {
            Student whatIf = copier.copy(student);
            blocker.getChange().applyTo(whatIf.getProfile());
            List<MatchResult> after = matchingService.findBuddies(whatIf, search);
            int goodAfter = countGood(after);
            if (goodAfter > goodBefore) {
                suggestions.add(new Suggestion(blocker.getChange(), blocker.getPeople(), goodBefore, goodAfter,
                        bestBefore, bestScore(after)));
            }
        }
        if (suggestions.isEmpty()) {
            return Advice.nobodyFits(goodBefore, countOthersNeeding(target, student), target.getCode());
        }
        Collections.sort(suggestions, new SuggestionComparator());
        while (suggestions.size() > maxSuggestions) {
            suggestions.remove(suggestions.size() - 1);
        }
        return Advice.withSuggestions(goodBefore, suggestions, target.getCode());
    }

    /** Returns the good-match score as a whole percentage (e.g. 60), for the page to show. */
    public long getGoodMatchPercent() {
        return Math.round(goodScore * 100);
    }

    /**
     * Applies one of the student's current suggestions to their real profile and saves it.
     *
     * @param student the signed-in student
     * @param key the suggestion's key, from the Apply button
     * @return the message for the success toast, with the new number of good matches
     * @throws BusinessRuleException if the key is not one of the student's current suggestions
     */
    public String apply(Student student, String key) {
        Advice advice = adviceFor(student);
        Suggestion chosen = null;
        if (advice != null && key != null) {
            for (Suggestion suggestion : advice.getSuggestions()) {
                if (suggestion.getChange().getKey().equals(key)) {
                    chosen = suggestion;
                }
            }
        }
        if (chosen == null) {
            throw new BusinessRuleException("That suggestion is no longer available. Your matches may have changed.");
        }
        PreferenceChange change = chosen.getChange();
        change.applyTo(student.getProfile());
        studentRepository.save(student);
        int goodNow = countGood(matchingService.findBuddies(student, targetSearch(targetCourse(student))));
        return change.getDoneMessage() + ". You now have " + goodNow
                + (goodNow == 1 ? " good match." : " good matches.");
    }

    /** Returns the results that scored just under good, each with the pair's criterion scores from both sides. */
    private List<NearMiss> nearMisses(Student student, List<MatchResult> results) {
        List<NearMiss> nearMisses = new ArrayList<>();
        for (MatchResult result : results) {
            double score = result.getMutualScore();
            if (score < nearMissScore || score >= goodScore) {
                continue;
            }
            // Their side: how well the advised student suits the candidate.
            MatchResult theirView = matchingService.evaluate(result.getCandidate(), student);
            Map<String, CriterionScore> theirSide = theirView == null
                    ? new LinkedHashMap<String, CriterionScore>() : theirView.getCriterionScores();
            nearMisses.add(new NearMiss(result.getCandidate(), result.getCriterionScores(), theirSide));
        }
        return nearMisses;
    }

    /** Returns how many results score at least the good-match score. */
    private int countGood(List<MatchResult> results) {
        int good = 0;
        for (MatchResult result : results) {
            if (result.getMutualScore() >= goodScore) {
                good++;
            }
        }
        return good;
    }

    /** Returns the highest mutual score in the results, or 0 if there are none. */
    private double bestScore(List<MatchResult> results) {
        double best = 0;
        for (MatchResult result : results) {
            best = Math.max(best, result.getMutualScore());
        }
        return best;
    }

    /** Returns how many other active students need the same target course. */
    private int countOthersNeeding(Course target, Student student) {
        int count = 0;
        for (Student other : studentRepository.findAll()) {
            if (other.getId().equals(student.getId()) || other.getStatus() != AccountStatus.ACTIVE) {
                continue;
            }
            Course theirTarget = targetCourse(other);
            if (theirTarget != null && theirTarget.equals(target)) {
                count++;
            }
        }
        return count;
    }

    /** Returns the student's target course, or null if they have none. */
    private Course targetCourse(Student student) {
        if (student.getProfile() == null || student.getProfile().getPreferences() == null) {
            return null;
        }
        return student.getProfile().getPreferences().getTargetCourse();
    }

    /** Returns the default Find buddies search: the target course, with no other filters. */
    private BuddySearch targetSearch(Course target) {
        BuddySearch search = new BuddySearch();
        search.setCourseCode(target.getCode());
        return search;
    }
}
