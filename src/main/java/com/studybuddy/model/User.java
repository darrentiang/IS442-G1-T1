package com.studybuddy.model;

/** Anyone with an account. Abstract: you are either a Student or an Administrator. */
public abstract class User {

    private final String id;
    private String name;
    private String email;
    private AccountStatus status;

    /** Creates a user with the given identity; new accounts start ACTIVE. */
    protected User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = AccountStatus.ACTIVE;
    }

    /** Returns the unique account id. */
    public String getId() {
        return id;
    }

    /** Returns the user's display name. */
    public String getName() {
        return name;
    }

    /** Changes the user's display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the user's email address. */
    public String getEmail() {
        return email;
    }

    /** Changes the user's email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns whether the account is active or suspended. */
    public AccountStatus getStatus() {
        return status;
    }

    /** Changes the account status (used by admin suspend/reactivate). */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /** Returns the role shown to people, e.g. "Student"; used instead of instanceof checks. */
    public abstract String getRoleLabel();
}
