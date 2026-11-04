# The Matching Engine

How Study Buddy Matcher turns a pool of students into a short, explained list of buddies. All of it is plain Java in `com.studybuddy.engine` (sub-packages `filter`, `criteria`, `strategy`); the class diagram is `docs/diagram-engine.mmd`.

## The pipeline

`MatchingEngine.findMatches(seeker, pool, context, config)` runs seven steps, one private method each:

| Step | What happens | Class |
|---|---|---|
| 1. Filter | Remove candidates who can never work (dealbreakers) and apply the search options | the 6 `CandidateFilter`s |
| 2. Score | Each enabled criterion scores seeker to candidate, 0.0 to 1.0, with a reason | the 8 `MatchCriterion`s |
| 3. Combine | Weighted fit both ways, then the harmonic mean of the two | `ReciprocalScorer` |
| 4. Adjust | Gently lower candidates who already have many pending requests | `CongestionPenalty` |
| 5. Rank | The configured strategy orders the results | the 3 `MatchingStrategy`s |
| 6. Cut | Drop results below `minimumScore`, keep the first `maxResults` | `MatchingEngine` |
| 7. Explain | Up to 2 reasons (highest weight x score), plus "N pending requests" if N >= 3 | `ExplanationBuilder` |

Filters (each gets the seeker, the candidate, the `MatchContext` and the `MatchingConfig`): `SelfFilter` (not yourself), `ActiveAccountFilter` (ACTIVE with a profile), `SearchFilter` (the course and goal searched for), `ExcludedFilter` (already connected, a pending request either way, declined you before, or you passed on them), `ModeFilter` (IN_PERSON keeps IN_PERSON or EITHER; ONLINE keeps ONLINE or EITHER; null or EITHER keeps everyone), `AvailabilityFilter` (when `minUsableHours` > 0, keeps candidates with at least that many usable hours together, counting blocks of `minSessionHours`).

`MatchingEngine.evaluate(seeker, candidate, config)` scores one pair for a candidate's profile page: steps 2, 3 and 7 only (score, combine, explain), with no filters, no congestion adjustment and no cut. It reuses the same private step methods as `findMatches`.

Strategies: `balanced` (mutual score), `availability-first` (usable hours, then mutual score), `course-first` (course score, then mutual score). Each extends `AbstractMatchingStrategy` (Template Method) and supplies a named `Comparator` class. An unknown strategy name falls back to `balanced`.

## The criteria

Each criterion scores from the **seeker's** side (S = seeker, C = candidate). Missing data scores 0.0 with no reason. A reason is shown only when the score is at least 0.6.

| Key | Label | Score |
|---|---|---|
| `course` | Course | 1.0 same target course; 0.6 C currently takes S's target; 0.3 any current course shared; else 0.0 |
| `availability` | Availability | min(1.0, usable hours / `targetUsableHours`), counting only shared blocks of at least `minSessionHours` |
| `studyMode` | Study mode | same 1.0; one is EITHER 0.8; IN_PERSON vs ONLINE 0.0 |
| `studyGoal` | Study goal | shared goals / S's number of goals |
| `groupSize` | Group size | same 1.0; one is EITHER 0.8; ONE_TO_ONE vs SMALL_GROUP 0.3 |
| `year` | Year | same 1.0; 1 apart 0.6; 2+ apart 0.2 |
| `intensity` | Intensity | 1.0 minus 0.5 per step apart on CASUAL, STEADY, GRIND |
| `sharedCourses` | Shared courses | min(1.0, n / 2), n = current courses in common other than S's target |

Adding a criterion means writing one class and adding one line to `MatchingEngine.createDefault()`.

## Three design choices

1. **Session-aware availability.** Only shared blocks long enough for a real session count (`AvailabilityGrid.commonBlocks`), because ten scattered single hours in common are worth less than one two-hour evening you can actually meet in.
2. **Reciprocal harmonic mean.** The mutual score is 2xy / (x + y) of the two one-way fits, because a match must work for both people, so a one-sided match (0.9 and 0.3 gives 0.45, not 0.6) is pulled down towards the weaker side.
3. **Congestion penalty.** The mutual score is divided by (1 + strength x pending requests), because otherwise the same strong profile tops everyone's list and is flooded with requests they cannot all accept.

## Default config

`MatchingConfig.defaults()`:

| Setting | Value |
|---|---|
| Strategy | `balanced` |
| Enabled criteria | all 8 |
| `maxResults` | 10 |
| `minimumScore` | 0.20 |
| `targetUsableHours` | 4 |
| `minSessionHours` | 2 |
| `congestionStrength` | 0.05 |

| Weight | course | availability | studyMode | studyGoal | groupSize | year | intensity | sharedCourses |
|---|---|---|---|---|---|---|---|---|
| Value | 0.20 | 0.25 | 0.12 | 0.15 | 0.08 | 0.07 | 0.08 | 0.05 |

Weights are normalised over the enabled criteria, so they need not sum to 1 after the admin switches some off.
