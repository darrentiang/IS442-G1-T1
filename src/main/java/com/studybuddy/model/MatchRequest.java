package com.studybuddy.model;

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

    /** Changes the request status. */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /** Returns a short description of this request for logs and debugging. */
    @Override
    public String toString() {
        return "MatchRequest[" + id + " " + from.getName() + " -> " + to.getName() + " " + status + "]";
    }
}
