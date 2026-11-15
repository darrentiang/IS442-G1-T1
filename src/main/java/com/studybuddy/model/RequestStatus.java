package com.studybuddy.model;

/** The lifecycle of a request: pending until accepted or declined. */
public enum RequestStatus {
    /** Sent and waiting for an answer. */
    PENDING,
    /** The receiver said yes. */
    ACCEPTED,
    /** The receiver said no. */
    DECLINED
}
