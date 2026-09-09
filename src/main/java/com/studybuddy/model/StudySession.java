package com.studybuddy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A planned meeting. Works for 2 buddies (from a Connection) or a whole group:
 * a session belongs to its participants, not to the group.
 */
public class StudySession {

    private final String id;
    private Course course;
    private int day;
    private int startHour;
    private int endHour;
    private String location;
    private List<Student> participants = new ArrayList<>();
    private Map<Student, Rsvp> rsvps = new HashMap<>();
    private SessionStatus status;

    /** Creates a session shell; it starts PROPOSED. */
    public StudySession(String id) {
        this.id = id;
        this.status = SessionStatus.PROPOSED;
    }

    /** Returns the unique session id. */
    public String getId() {
        return id;
    }

    /** Returns the course being studied. */
    public Course getCourse() {
        return course;
    }

    /** Changes the course being studied. */
    public void setCourse(Course course) {
        this.course = course;
    }

    /** Returns the day in grid coordinates (0 = Monday). */
    public int getDay() {
        return day;
    }

    /** Changes the day in grid coordinates (0 = Monday). */
    public void setDay(int day) {
        this.day = day;
    }

    /** Returns the starting hour index (0 = 08:00). */
    public int getStartHour() {
        return startHour;
    }

    /** Changes the starting hour index (0 = 08:00). */
    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    /** Returns the ending hour index. */
    public int getEndHour() {
        return endHour;
    }

    /** Changes the ending hour index. */
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    /** Returns the meeting location. */
    public String getLocation() {
        return location;
    }

    /** Changes the meeting location. */
    public void setLocation(String location) {
        this.location = location;
    }

    /** Returns the students this session belongs to. */
    public List<Student> getParticipants() {
        return participants;
    }

    /** Replaces the students this session belongs to. */
    public void setParticipants(List<Student> participants) {
        this.participants = participants;
    }

    /** Returns each participant's RSVP answer. */
    public Map<Student, Rsvp> getRsvps() {
        return rsvps;
    }

    /** Replaces the RSVP answers. */
    public void setRsvps(Map<Student, Rsvp> rsvps) {
        this.rsvps = rsvps;
    }

    /** Returns the session status: PROPOSED, CONFIRMED or PAST. */
    public SessionStatus getStatus() {
        return status;
    }

    /** Changes the session status. */
    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    /** Returns a short description of this session for logs and debugging. */
    @Override
    public String toString() {
        return "StudySession[" + id + " day " + day + " " + startHour + "-" + endHour + " " + status + "]";
    }
}
