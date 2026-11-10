# BOSS Advisor

Our X factor: when a student has too few good study buddy matches, **Find buddies tells them the one preference change that would unlock more**, shows what they would gain and give up, and applies it in one click.

| | |
|---|---|
| Where | Find buddies (`/buddies`), a card above the results |
| Who sees it | A signed-in student with fewer than 3 good matches for their own target course |
| Code | `com.studybuddy.engine.advisor` (13 classes), `BossAdvisorService`, `BuddyController` |
| New dependencies | None |

---

## 1. The problem

Find buddies ranks other students by a **mutual fit** score: how well each pair suits each other, from both sides. Most students get a healthy list. Some do not: their best results score 40 to 59%, close but not good, and they have no idea why or what to do about it.

We call these students **stranded**. A student is stranded when their Find buddies list for their target course has **fewer than 3 good matches**, where a good match scores **60% or more**.

There are two reasons a student can be stranded:

1. **Their own preferences shut people out.** For example: in person only, or free only on Monday mornings. People who would otherwise suit them lose points on that one preference and fall just under 60%. **One change can fix this.**
2. **Nobody fits.** Very few other students need that course this term. No preference change helps.

A third reason, being crowded out by a few popular students, is left out on purpose: only the admin's matching settings can fix it, not the student.

Without BOSS Advisor, a stranded student can only guess: change something in their profile, search again, and hope. Most will not bother.

## 2. The goal

Matching platforms are judged on three measures. BOSS Advisor targets the first and does not touch the other two, which the matching engine already handles.

| Measure | Plain meaning | In our app |
|---|---|---|
| **Reach** | How many people end up with a real, usable match | Students with at least 3 good matches. **BOSS Advisor raises this.** |
| Spread | Attention is shared, not piled onto a few | Already handled by `CongestionPenalty` |
| Quality | Matches work for both sides | Already handled by the harmonic mean in `ReciprocalScorer` |

**Goal:** help as many students as possible reach a list of good, mutual matches, by telling each stranded student exactly which change would help, and only when it really does.

## 3. What the student sees

The card appears at the top of Find buddies, **only** for a stranded student and **only** on their plain target-course search (not when they have filtered by mode, goal or free time, since advice about a temporary filter would be confusing).

Each suggestion shows four things, and the best one gets the primary Apply button:

1. **The exact change:** "If you're free Sun 14:00–16:00, add it to your free time"
2. **The gain, checked by the real engine:** "+4 good matches · best fit 57% → 69%"
3. **Why:** "10 people almost matched you but were held back by free time"
4. **What you give up:** "Only add it if you're really free then; buddies will expect you"

Up to 3 suggestions are shown, best first. A suggestion only appears if it adds at least one good match.

After **Apply**, the profile changes, the list refreshes and a toast confirms the result: "Added Sun 14:00–16:00 to your free time. You now have 4 good matches." The card then disappears, because the student is no longer stranded.

**When nothing helps**, the card says so honestly and points somewhere useful:

> No single change gets you more good matches. Only 4 other students need STAT101 this term, so a study group might help. [See STAT101 study groups]

### What it can suggest

Only things a student can choose for themselves:

| Kind of change | Example wording | Trade-off shown |
|---|---|---|
| Add a free time block | "If you're free Thu 19:00–21:00, add it to your free time" | "Only add it if you're really free then; buddies will expect you." |
| Widen study mode | "Change study mode from In person to In person or online" | "You may be matched with people who prefer to study online." |
| Widen group size | "Change group size from One-to-one to One-to-one or small group" | "You may be invited to study in small groups, not only one-to-one." |
| Add a study goal | "Add Exam prep to your study goals" | "Some of your sessions may cover exam prep as well." |

It never suggests changing the target course, courses taken or completed, year, school, programme or contact number. Time blocks are worded "if you're free" because an empty slot in the grid might be a class: the student decides.

### On the seed data

Run against all 50 seeded students:

| Outcome | Students |
|---|---|
| Enough good matches, no card | 31 |
| Stranded, given verified suggestions | 18 |
| Stranded, nobody fits (honest message) | 1 (S048, Hakim Osman, STAT101) |

Best demo persona: **S045, Naveen Iyer** (IS212). 0 good matches → **4** after adding Sun 14:00–16:00. Other strong cases: S017 Amanda Lee (2 → 7), S010 Arjun Menon (1 → 5).

## 4. How it works

### The idea in one line

> Find the people who **almost** matched the student, work out **which preference held most of them back**, try relaxing that preference on a **copy** of the student, and keep the change only if the **real** Find buddies search confirms it adds good matches.

### Step by step

```
Student opens Find buddies (own target course, no filters)
 1. Normal search                      → their ranked results
 2. Count good matches (≥ 60%)         → 3 or more? stop, no card
 3. Take the near misses (40–59%)
 4. Diagnose each near miss from BOTH sides: which preference cost the most points?
 5. Tally the blockers and turn each into an exact change
 6. For each change: copy the student → apply it → rerun the same search → count good matches
 7. Keep changes that gain ≥ 1, best first, top 3 → the card
Apply → re-run 1–7, find that change by its key → apply to the REAL profile → save → toast
```

### Step 4: two-sided diagnosis

A match is mutual: the score combines how well the candidate suits the student **and** how well the student suits the candidate (the harmonic mean pulls the total towards the weaker side). So each near miss is scored both ways:

- **Her side:** the criterion scores already on the search result.
- **Their side:** one extra call to the engine's existing `evaluate(candidate, student)`, which scores the pair from the candidate's point of view.

For each preference she can change (study mode, free time, study goal, group size):

```
points lost = weight × (1 − her score) + weight × (1 − their score)
```

The preference with the most points lost is that person's **blocker**. Looking at both sides catches blockers that one side alone would miss, for example when the candidate wants a goal she does not list.

### Step 5: from blocker to an exact change

| Blocker | Change | How the exact value is chosen |
|---|---|---|
| Study mode | Widen mode to Either | Only if she is not already Either |
| Group size | Widen group size to Either | Only if she is not already Either |
| Study goal | Add one goal | The goal most common among those near misses that she does not have |
| Free time | Add a time block (length = the admin's minimum session hours) | The block when most of those near misses are free and she is not yet |

### Step 6: verification

The diagnosis is an estimate. Every suggestion is then **checked with the real engine**: the change is applied to a copy of the student, the same Find buddies search is run on the copy, and the good matches are counted. A suggestion that does not add a good match is dropped. **Every number on the card comes from the real engine.**

### Why near-miss diagnosis, not trying everything

The simplest approach would be to try every possible change (about 90 time blocks plus mode, group size and goals) and keep the best. Near-miss diagnosis looks at the data first instead:

| | Try everything | Near-miss diagnosis (what we built) |
|---|---|---|
| Engine reruns | ~95 | ~3 (one per blocker) |
| Explains why | No | Yes: "10 people were held back by free time" |
| Numbers shown | Exact | Exact (verified) |

## 5. Design

### Classes by layer

```
Controller   BuddyController            GET /buddies adds the advice; POST /buddies/boss-advisor/apply
Service      BossAdvisorService         runs the flow, applies the chosen change
Engine       engine.advisor             change classes, diagnosis, copier, results (plain Java)
Repository   StudentRepository          (existing) saves the profile after Apply
Model        Student, StudentProfile, StudyPreferences, AvailabilityGrid   (existing, unchanged)
```

### `engine.advisor` (plain Java: no Spring, no streams, no lambdas, no `Optional`)

| Class | Job | OOP point |
|---|---|---|
| `PreferenceChange` (abstract) | One change a student could make: `getKey`, `appliesTo`, `applyTo`, `describe`, `getDoneMessage`, `getTradeOff`, `getBlockerLabel` | Abstraction |
| `WidenModeChange` | Study mode → Either | Inheritance, polymorphism |
| `WidenFormatChange` | Group size → Either | Inheritance, polymorphism |
| `AddGoalChange` | Adds one study goal | Inheritance, polymorphism |
| `AddTimeSlotChange` | Marks a block of hours as free | Inheritance, polymorphism |
| `NearMissDiagnosis` | Works out each near miss's blocker from both sides, tallies them, builds the matching changes | Single responsibility: all the "why" logic in one place |
| `NearMiss` | A near-miss candidate with the pair's criterion scores from both sides | Value object, read-only views |
| `Blocker` | A change plus how many near misses it held back | Value object |
| `WhatIfCopier` | Deep-copies a student so a change can be tried without touching the real one | Encapsulation, defensive copies |
| `Suggestion` | A verified change: good matches before and after, best fit before and after, the reason | Immutable value object |
| `SuggestionComparator` | Orders suggestions: most good matches gained, then the biggest rise in best fit | `Comparator` implementation (no lambdas) |
| `Advice` | The answer for one student: needs help or not, the count, the suggestions, or "nobody fits" | Factory methods, read-only list |

### Design principles to point to

- **Polymorphism and Open/Closed.** The service never asks which kind of change it has; it calls `applyTo`, `describe` and `getKey` on a `PreferenceChange`. Adding a new kind of suggestion means writing one new subclass. Nothing else changes.
- **Reuse, not duplication.** BOSS Advisor contains no matching logic of its own. It calls the existing `MatchingService.findBuddies` and `evaluate`, so its numbers can never disagree with the list the student sees.
- **Single responsibility.** Diagnosing (`NearMissDiagnosis`), copying (`WhatIfCopier`), ordering (`SuggestionComparator`) and orchestrating (`BossAdvisorService`) are separate classes.
- **Encapsulation.** Previews run on copies. The real profile changes only through `apply`, after the student clicks.
- **Domain firewall.** Everything in `engine.advisor` is plain Java, as `AGENTS.md` requires.

### Key implementation details

**Copying a student (`WhatIfCopier`).** It builds a new `Student` with the **same id**, name, email and status, then a new `StudentProfile`, `StudyPreferences` and `AvailabilityGrid`, copied field by field through the existing getters and setters (no methods added to shared classes). The same id matters: the search leaves out the student themselves (`SelfFilter` compares ids), their buddies, pending requests and people they passed on, all by id, so the copy gets exactly the same exclusions as the real student. The contact number is not copied: matching never uses it, and it has no getter by design.

**Reusing the search unchanged.** `BossAdvisorService` calls `matchingService.findBuddies(copy, search)` with a search on the target course only. Because the copy keeps the same id, `MatchingService` needed no changes at all.

**Apply is safe against forged input.** The Apply button sends only a key, such as `slot:6:6:2` or `mode:EITHER`. `apply` re-runs the advisor and looks for a current suggestion with that key. If there is none (a stale or forged key) it throws `BusinessRuleException`, and the controller shows an error toast. So only changes the advisor itself produced can ever be applied.

**Change keys.**

| Change | Key format | Example |
|---|---|---|
| Time block | `slot:<day>:<start index>:<hours>` (day 0 = Monday, index 0 = 08:00) | `slot:3:11:2` = Thu 19:00–21:00 |
| Study mode | `mode:EITHER` | |
| Group size | `format:EITHER` | |
| Study goal | `goal:<StudyGoal name>` | `goal:EXAM_PREP` |

### Request flow

**Viewing Find buddies**

1. `GET /buddies` → `BuddyController.showBuddies` builds the normal results, as before.
2. If the search is the plain target-course search, it also calls `BossAdvisorService.adviceFor(student)` and adds `advice` and `advisorGoodPercent` to the model.
3. `find-buddies.html` shows the card when `advice.needsHelp` is true: the suggestions, or the "nobody fits" message.

**Applying a suggestion**

1. `POST /buddies/boss-advisor/apply` with `key`.
2. `BossAdvisorService.apply(student, key)` re-runs the advice, finds the key, calls `change.applyTo(student.getProfile())`, saves through `StudentRepository`, and returns the toast message with the new count.
3. Post/Redirect/Get back to `/buddies` with `Toasts.success` (or `Toasts.error` for an unknown key).

## 6. Settings

In `application.properties`, read once in `BossAdvisorService`'s constructor through `Environment`, like the other services:

| Setting | Default | Meaning |
|---|---|---|
| `studybuddy.advisor.good-match-score` | `0.60` | A match at or above this is "good" |
| `studybuddy.advisor.near-miss-score` | `0.40` | Results from here up to the good score are near misses |
| `studybuddy.advisor.enough-good-matches` | `3` | Fewer good matches than this means stranded |
| `studybuddy.advisor.max-suggestions` | `3` | Most suggestions shown at once |

The length of a suggested time block is the admin's **minimum session hours** from the matching configuration (2 by default).

## 7. Rules it follows

- **No AI and no randomness.** Every number comes from the real engine and the student's real profile.
- **It never changes a profile on its own.** Only Apply changes data, and only for a change the advisor just verified.
- **Previews never touch real data.** Every "what if" runs on a copy.
- **No changes to shared classes** (`FOUNDATION.md` section 3), and no new dependencies.
- **Honest when it cannot help.** No invented nudges: if no change adds a good match, it says so and points to study groups.

## 8. Limitations

- **One change at a time.** It never combines two changes. That keeps the advice clear and the search small.
- **Near misses only.** People below 40% are not diagnosed; a change that would win back someone far below "good" is rarely the best fix.
- **Limited to the result list.** Find buddies keeps at most the admin's "max results" (10 by default), so the advisor only sees near misses within that list.
- **Best fit can dip.** A suggestion can add good matches while the single best score falls slightly (e.g. 68% → 67%). The card shows the real numbers rather than hiding this.
- **Crowded-out students are not covered.** If someone is stranded because a few popular students attract everyone, only the admin's congestion setting can help.
- **Tuning.** With the defaults, 19 of 50 seeded students (38%) are stranded. The thresholds are settings and can be tuned.

## 9. Testing

Checked by hand in a browser on a fresh server:

| Case | Expected | Result |
|---|---|---|
| Stranded student opens Find buddies | Card with suggestions, best first | ✅ |
| Apply the top suggestion | Profile updated, list refreshed, toast with the new count, card gone | ✅ (S045: 0 → 4) |
| Forged key sent to Apply | Error toast, nothing changes | ✅ |
| Student with enough good matches | No card | ✅ (S001) |
| Stranded student with a filter on | No card | ✅ (S017, Online only) |
| Nobody fits | Honest message with a link to that course's groups | ✅ (S048) |
| Every page, desktop and phone, student and admin | Loads, no sideways scroll | ✅ (48 page loads) |

## 10. Demo script (about 30 seconds)

1. Sign in as **S045 (Naveen Iyer)** and open **Buddies**. Point at the card: "0 good matches. BOSS Advisor found 10 people who almost matched him, and free time held them back."
2. Read the suggestion: "If you're free Sun 14:00–16:00: +4 good matches, best fit 57% → 69%."
3. Click **Apply**. The toast says "You now have 4 good matches" and the list now shows them. The card is gone.
4. Optional: switch to **S048 (Hakim Osman)** to show the honest case: no change helps, so it points to STAT101 study groups.

> "BOSS Advisor finds the people who almost matched you, works out which preference cost you each one, from both sides, and suggests the change that wins back the most of them. Then it confirms with our real engine, so every number you see is real."

## 11. Questions to be ready for

**Why not just try every possible change?** We could, and with about 95 options it would still be fast. Diagnosing near misses first is more useful: it explains *why* the student is stranded, and it only needs about 3 engine reruns. Verification keeps the numbers exact either way.

**Why look at both sides of each pair?** The score is mutual. If we only looked at her side, we would miss blockers on the other person's side, such as a goal they want that she does not list.

**Could the advice be wrong?** The diagnosis is an estimate, but every suggestion is verified by rerunning the real search on a copy. If the gain is not real, the suggestion is dropped.

**How do you add a new kind of suggestion?** Write one subclass of `PreferenceChange` and teach `NearMissDiagnosis` which criterion it relaxes. The service and the page need no changes.

**Can a student apply something the advisor did not suggest?** No. Apply only accepts a key from the advisor's current suggestions; anything else is refused with an error toast.

## 12. Possible next steps

- **Admin reach panel:** a card on the admin overview showing "Well matched: 31 of 50", with the stranded students split into "can fix it with one change" and "nobody fits". It would reuse `adviceFor` per student, so the admin number and the student card could never disagree.
- **More kinds of change**, such as widening study intensity, each as one new `PreferenceChange` subclass.
