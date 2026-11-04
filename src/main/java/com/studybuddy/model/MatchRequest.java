package com.studybuddy.model;

import java.util.Objects;

/** A buddy request from one student to another. */
public class MatchRequest {

    private final String id;
    private final Student from;
    private final Student to;
    private String message;
    private RequestStatus status;

    /** Creates a request; it starts PENDING. */
    public MatchRequest(String id, Student from, Student to) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.status = RequestStatus.PENDING;
    }

    /** Returns the unique request id. */
    public String getId() {
        return id;
    }

    /** Returns the student who sent the request. */
    public Student getFrom() {
        return from;
    }

    /** Returns the student the request was sent to. */
    public Student getTo() {
        return to;
    }

    /** Returns the optional message attached to the request, or null. */
    public String getMessage() {
        return message;
    }

    /** Attaches or changes the optional message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Returns the request status: PENDING, ACCEPTED or DECLINED. */
    public RequestStatus getStatus() {
        return status;
    }

    /** Marks the request ACCEPTED; throws RequestAlreadyHandledException unless it is PENDING. Does not create the connection. */
    public void accept() {
        requirePending();
        status = RequestStatus.ACCEPTED;
    }

    /** Marks the request DECLINED; throws RequestAlreadyHandledException unless it is PENDING. */
    public void decline() {
        requirePending();
        status = RequestStatus.DECLINED;
    }

    /** Throws RequestAlreadyHandledException if this request has already been answered. */
    private void requirePending() {
        if (status != RequestStatus.PENDING) {
            throw new RequestAlreadyHandledException("This request was already handled");
        }
    }

    /** Returns a short description of this request for logs and debugging. */
    @Override
    public String toString() {
        return "MatchRequest[" + id + " " + from.getName() + " -> " + to.getName() + " " + status + "]";
    }

    /** Two buddy requests are equal when they have the same id. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MatchRequest)) {
            return false;
        }
        MatchRequest that = (MatchRequest) other;
        return Objects.equals(id, that.id);
    }

    /** Returns a hash code from the id, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
