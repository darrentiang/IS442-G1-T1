package com.studybuddy.model;

/** A request by a student to join a group. Same lifecycle as MatchRequest. */
public class JoinRequest {

    private final String id;
    private final Student student;
    private final StudyGroup group;
    private RequestStatus status;

    /** Creates a join request; it starts PENDING. */
    public JoinRequest(String id, Student student, StudyGroup group) {
        this.id = id;
        this.student = student;
        this.group = group;
        this.status = RequestStatus.PENDING;
    }

    /** Returns the unique request id. */
    public String getId() {
        return id;
    }

    /** Returns the student asking to join. */
    public Student getStudent() {
        return student;
    }

    /** Returns the group being asked to join. */
    public StudyGroup getGroup() {
        return group;
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
        return "JoinRequest[" + id + " " + student.getName() + " -> " + group.getName() + " " + status + "]";
    }
}
