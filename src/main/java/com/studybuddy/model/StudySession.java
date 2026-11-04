package com.studybuddy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    /** Returns the students this session belongs to, as a read-only view. */
    public List<Student> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    /** Replaces the students this session belongs to with a copy of the given list. */
    public void setParticipants(List<Student> participants) {
        this.participants = new ArrayList<>(participants);
    }

    /** Returns each participant's RSVP answer, as a read-only view. */
    public Map<Student, Rsvp> getRsvps() {
        return Collections.unmodifiableMap(rsvps);
    }

    /** Replaces the RSVP answers with a copy of the given map. */
    public void setRsvps(Map<Student, Rsvp> rsvps) {
        this.rsvps = new HashMap<>(rsvps);
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

    /** Two sessions are equal when they have the same id. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StudySession)) {
            return false;
        }
        StudySession that = (StudySession) other;
        return Objects.equals(id, that.id);
    }

    /** Returns a hash code from the id, consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
