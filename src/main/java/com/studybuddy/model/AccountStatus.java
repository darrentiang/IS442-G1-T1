package com.studybuddy.model;

/** Whether a user account may currently use the system. */
public enum AccountStatus {
    /** The account can sign in and use the system. */
    ACTIVE,
    /** An administrator has blocked the account from signing in. */
    SUSPENDED
}
