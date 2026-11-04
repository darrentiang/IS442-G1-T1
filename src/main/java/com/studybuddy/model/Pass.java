package com.studybuddy.model;

import java.time.LocalDate;

/** Recorded when a student clicks Pass on Find Buddies; the candidate is hidden from that student's future results. */
public class Pass {

    private final Student seeker;
    private final Student candidate;
    private final LocalDate date;

    /** Records that the seeker passed on the candidate on the given date. */
    public Pass(Student seeker, Student candidate, LocalDate date) {
        this.seeker = seeker;
        this.candidate = candidate;
        this.date = date;
    }

    /** Returns the student who clicked Pass. */
    public Student getSeeker() {
        return seeker;
    }

    /** Returns the student who was passed on. */
    public Student getCandidate() {
        return candidate;
    }

    /** Returns the date of the pass. */
    public LocalDate getDate() {
        return date;
    }

    /** Returns a short description of this pass for logs and debugging. */
    @Override
    public String toString() {
        return "Pass[" + seeker.getName() + " passed on " + candidate.getName() + " " + date + "]";
    }
}
