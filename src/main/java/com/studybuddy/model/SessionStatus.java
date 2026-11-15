package com.studybuddy.model;

/** The lifecycle of a study session: proposed, then confirmed, then past. */
public enum SessionStatus {
    /** Proposed and waiting for answers. */
    PROPOSED,
    /** Confirmed by the proposer. */
    CONFIRMED,
    /** Marked as done by the proposer. */
    PAST
}
