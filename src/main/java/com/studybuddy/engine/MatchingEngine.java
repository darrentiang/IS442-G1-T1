package com.studybuddy.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.studybuddy.engine.criteria.AvailabilityCriterion;
import com.studybuddy.engine.criteria.CourseCriterion;
import com.studybuddy.engine.criteria.GroupSizeCriterion;
import com.studybuddy.engine.criteria.IntensityCriterion;
import com.studybuddy.engine.criteria.SharedCoursesCriterion;
import com.studybuddy.engine.criteria.StudyGoalCriterion;
import com.studybuddy.engine.criteria.StudyModeCriterion;
import com.studybuddy.engine.criteria.YearCriterion;
import com.studybuddy.engine.filter.ActiveAccountFilter;
import com.studybuddy.engine.filter.AvailabilityFilter;
import com.studybuddy.engine.filter.ExcludedFilter;
import com.studybuddy.engine.filter.ModeFilter;
import com.studybuddy.engine.filter.SearchFilter;
import com.studybuddy.engine.filter.SelfFilter;
import com.studybuddy.engine.strategy.AvailabilityFirstStrategy;
import com.studybuddy.engine.strategy.BalancedStrategy;
import com.studybuddy.engine.strategy.CourseFirstStrategy;
import com.studybuddy.engine.strategy.MatchingStrategy;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;

/**
 * Finds and ranks study buddies for one student: filter, score, combine both ways, adjust for
 * congestion, rank, cut and explain. Knows nothing about Spring or storage.
 */
public class MatchingEngine {

    private static final double NOT_YET_COMBINED = 0.0;
    private static final int NO_HOURS = 0;
    private static final int NO_HOUR_LIMIT = 0;

    private final List<CandidateFilter> filters;
    private final Map<String, MatchCriterion> criteria = new LinkedHashMap<>();
    private final Map<String, MatchingStrategy> strategies = new LinkedHashMap<>();
    private final ReciprocalScorer scorer = new ReciprocalScorer();
    private final CongestionPenalty congestionPenalty = new CongestionPenalty();
    private final ExplanationBuilder explanationBuilder = new ExplanationBuilder();

    /**
     * Creates an engine from its parts. Criteria and strategies are kept in the order given.
     *
     * @param filters the dealbreaker checks, applied in order
     * @param criteria the scoring rules, keyed by their names
     * @param strategies the ranking orders, keyed by their names
     */
    public MatchingEngine(List<CandidateFilter> filters, List<MatchCriterion> criteria,
            List<MatchingStrategy> strategies) {
        this.filters = new ArrayList<>(filters);
        for (MatchCriterion criterion : criteria) {
            this.criteria.put(criterion.getName(), criterion);
        }
        for (MatchingStrategy strategy : strategies) {
            this.strategies.put(strategy.getName(), strategy);
        }
    }

    /**
     * Builds the engine the app uses: 6 filters, 8 criteria, 3 strategies. This is the ONE
     * place they are assembled, so adding a criterion means writing one class and adding one
     * line here; nothing else changes (the open/closed principle).
     *
     * @return the fully assembled engine
     */
    public static MatchingEngine createDefault() {
        List<CandidateFilter> filters = new ArrayList<>();
        filters.add(new SelfFilter());
        filters.add(new ActiveAccountFilter());
        filters.add(new SearchFilter());
        filters.add(new ExcludedFilter());
        filters.add(new ModeFilter());
        filters.add(new AvailabilityFilter());

        List<MatchCriterion> criteria = new ArrayList<>();
        criteria.add(new CourseCriterion());
        criteria.add(new AvailabilityCriterion());
        criteria.add(new StudyModeCriterion());
        criteria.add(new StudyGoalCriterion());
        criteria.add(new GroupSizeCriterion());
        criteria.add(new YearCriterion());
        criteria.add(new IntensityCriterion());
        criteria.add(new SharedCoursesCriterion());

        List<MatchingStrategy> strategies = new ArrayList<>();
        strategies.add(new BalancedStrategy());
        strategies.add(new AvailabilityFirstStrategy());
        strategies.add(new CourseFirstStrategy());

        return new MatchingEngine(filters, criteria, strategies);
    }

    /**
     * Finds the best study buddies for the seeker from the pool.
     *
     * @param seeker the student looking for a buddy
     * @param pool every student who might be suggested
     * @param context the search options, who to leave out, and pending request counts
     * @param config the admin's settings
     * @return the results, best first; empty if the seeker has no profile
     */
    public List<MatchResult> findMatches(Student seeker, List<Student> pool, MatchContext context,
            MatchingConfig config) {
        if (seeker.getProfile() == null) {
            return new ArrayList<>();
        }
        // 1. Filter: remove candidates who can never work
        List<Student> candidates = filter(seeker, pool, context, config);
        // 2. Score: each enabled criterion scores seeker to candidate, with a reason
        List<MatchResult> scored = score(seeker, candidates, context, config);
        // 3. Combine: weighted fit both ways, then the harmonic mean of the two
        List<MatchResult> combined = combine(seeker, scored, config);
        // 4. Adjust: gently lower candidates who already have many pending requests
        List<MatchResult> adjusted = adjust(combined, config);
        // 5. Rank: the configured strategy orders the results
        List<MatchResult> ranked = rank(adjusted, config);
        // 6. Cut: drop results below minimumScore, keep the first maxResults
        List<MatchResult> cut = cut(ranked, config);
        // 7. Explain: attach the top reasons for each result
        return explain(cut, config);
    }

    /**
     * Scores one pair on its own, for a candidate's profile page: score, combine and explain,
     * with no filters, no congestion adjustment and no cut.
     *
     * @param seeker the student looking at the profile
     * @param candidate the student being looked at
     * @param config the admin's settings
     * @return the result for this pair, or null if either student has no profile
     */
    public MatchResult evaluate(Student seeker, Student candidate, MatchingConfig config) {
        if (seeker.getProfile() == null || candidate.getProfile() == null) {
            return null;
        }
        List<Student> single = new ArrayList<>();
        single.add(candidate);
        MatchContext noSearch = new MatchContext(null, null, null, NO_HOUR_LIMIT, null, null);
        List<MatchResult> scored = score(seeker, single, noSearch, config);
        List<MatchResult> combined = combine(seeker, scored, config);
        List<MatchResult> explained = explain(combined, config);
        return explained.get(0);
    }

    /** Returns the registered criterion names, in registration order. */
    public List<String> getCriterionNames() {
        return new ArrayList<>(criteria.keySet());
    }

    /** Returns the registered strategy names, in registration order. */
    public List<String> getStrategyNames() {
        return new ArrayList<>(strategies.keySet());
    }

    /** Returns the display name of a criterion, or the name itself if it is not registered. */
    public String getCriterionLabel(String name) {
        MatchCriterion criterion = criteria.get(name);
        if (criterion == null) {
            return name;
        }
        return criterion.getLabel();
    }

    /** Returns the display name of a strategy, or the name itself if it is not registered. */
    public String getStrategyLabel(String name) {
        MatchingStrategy strategy = strategies.get(name);
        if (strategy == null) {
            return name;
        }
        return strategy.getLabel();
    }

    /** Step 1: keeps only the candidates every filter accepts. */
    private List<Student> filter(Student seeker, List<Student> pool, MatchContext context,
            MatchingConfig config) {
        List<Student> kept = new ArrayList<>();
        for (Student candidate : pool) {
            if (passesAllFilters(seeker, candidate, context, config)) {
                kept.add(candidate);
            }
        }
        return kept;
    }

    /** Returns true if no filter rejects the candidate. */
    private boolean passesAllFilters(Student seeker, Student candidate, MatchContext context,
            MatchingConfig config) {
        for (CandidateFilter filter : filters) {
            if (!filter.accepts(seeker, candidate, context, config)) {
                return false;
            }
        }
        return true;
    }

    /** Step 2: scores each candidate from the seeker's side and records usable hours and pending requests. */
    private List<MatchResult> score(Student seeker, List<Student> candidates, MatchContext context,
            MatchingConfig config) {
        List<MatchCriterion> enabled = enabledCriteria(config);
        List<MatchResult> scored = new ArrayList<>();
        for (Student candidate : candidates) {
            StudentProfile seekerProfile = seeker.getProfile();
            StudentProfile candidateProfile = candidate.getProfile();
            Map<String, CriterionScore> seekerSide = scorer.scoreSide(seekerProfile, candidateProfile, enabled, config);
            int usableHours = usableHours(seekerProfile, candidateProfile, config);
            int pending = context.getPendingIncoming(candidate.getId());
            scored.add(buildResult(candidate, NOT_YET_COMBINED, NOT_YET_COMBINED, NOT_YET_COMBINED,
                    seekerSide, usableHours, pending, new ArrayList<String>()));
        }
        return scored;
    }

    /** Step 3: works out both one-way fits and the mutual score for each result. */
    private List<MatchResult> combine(Student seeker, List<MatchResult> scored, MatchingConfig config) {
        List<MatchCriterion> enabled = enabledCriteria(config);
        List<MatchResult> combined = new ArrayList<>();
        for (MatchResult result : scored) {
            StudentProfile candidateProfile = result.getCandidate().getProfile();
            Map<String, CriterionScore> candidateSide =
                    scorer.scoreSide(candidateProfile, seeker.getProfile(), enabled, config);
            double fitForSeeker = scorer.weightedFit(result.getCriterionScores(), config);
            double fitForCandidate = scorer.weightedFit(candidateSide, config);
            double mutual = scorer.mutualScore(fitForSeeker, fitForCandidate);
            combined.add(buildResult(result.getCandidate(), mutual, fitForSeeker, fitForCandidate,
                    result.getCriterionScores(), result.getUsableHours(), result.getPendingRequests(),
                    result.getReasons()));
        }
        return combined;
    }

    /** Step 4: applies the congestion penalty to each mutual score. */
    private List<MatchResult> adjust(List<MatchResult> combined, MatchingConfig config) {
        List<MatchResult> adjusted = new ArrayList<>();
        for (MatchResult result : combined) {
            double mutual = congestionPenalty.apply(result.getMutualScore(), result.getPendingRequests(),
                    config.getCongestionStrength());
            adjusted.add(buildResult(result.getCandidate(), mutual, result.getFitForSeeker(),
                    result.getFitForCandidate(), result.getCriterionScores(), result.getUsableHours(),
                    result.getPendingRequests(), result.getReasons()));
        }
        return adjusted;
    }

    /** Step 5: orders the results with the configured strategy, or balanced if the name is unknown. */
    private List<MatchResult> rank(List<MatchResult> adjusted, MatchingConfig config) {
        MatchingStrategy strategy = strategies.get(config.getActiveStrategyName());
        if (strategy == null) {
            strategy = strategies.get(BalancedStrategy.NAME);
        }
        if (strategy == null) {
            return new ArrayList<>(adjusted);
        }
        return strategy.rank(adjusted);
    }

    /** Step 6: drops results below the minimum score and keeps at most maxResults. */
    private List<MatchResult> cut(List<MatchResult> ranked, MatchingConfig config) {
        List<MatchResult> kept = new ArrayList<>();
        for (MatchResult result : ranked) {
            if (kept.size() >= config.getMaxResults()) {
                break;
            }
            if (result.getMutualScore() >= config.getMinimumScore()) {
                kept.add(result);
            }
        }
        return kept;
    }

    /** Step 7: attaches the top reasons to each result. */
    private List<MatchResult> explain(List<MatchResult> cut, MatchingConfig config) {
        List<MatchResult> explained = new ArrayList<>();
        for (MatchResult result : cut) {
            List<String> reasons = explanationBuilder.build(result.getCriterionScores(),
                    result.getPendingRequests(), config);
            explained.add(buildResult(result.getCandidate(), result.getMutualScore(), result.getFitForSeeker(),
                    result.getFitForCandidate(), result.getCriterionScores(), result.getUsableHours(),
                    result.getPendingRequests(), reasons));
        }
        return explained;
    }

    /** Returns the registered criteria that the config switches on, in registration order. */
    private List<MatchCriterion> enabledCriteria(MatchingConfig config) {
        List<MatchCriterion> enabled = new ArrayList<>();
        Set<String> enabledNames = config.getEnabledCriteria();
        if (enabledNames == null) {
            return enabled;
        }
        for (MatchCriterion criterion : criteria.values()) {
            if (enabledNames.contains(criterion.getName())) {
                enabled.add(criterion);
            }
        }
        return enabled;
    }

    /** Returns the hours both are free in blocks long enough for a session. */
    private int usableHours(StudentProfile seeker, StudentProfile candidate, MatchingConfig config) {
        AvailabilityGrid seekerGrid = seeker.getAvailability();
        AvailabilityGrid candidateGrid = candidate.getAvailability();
        if (seekerGrid == null || candidateGrid == null) {
            return NO_HOURS;
        }
        return seekerGrid.usableHours(candidateGrid, config.getMinSessionHours());
    }

    /** The one place a MatchResult is built, so every step creates results the same way. */
    private MatchResult buildResult(Student candidate, double mutualScore, double fitForSeeker,
            double fitForCandidate, Map<String, CriterionScore> criterionScores, int usableHours,
            int pendingRequests, List<String> reasons) {
        return new MatchResult(candidate, mutualScore, fitForSeeker, fitForCandidate, criterionScores,
                usableHours, pendingRequests, reasons);
    }
}
