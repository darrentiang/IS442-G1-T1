package com.studybuddy.service;

/**
 * Raw values from the "Plan a session" form, all as text; SessionService validates and converts
 * them. Exactly one of buddy (a connection id) or group (a group id) says who the session is with.
 */
public class SessionForm {

    private String buddy;
    private String group;
    private String courseCode;
    private String day;
    private String startHour;
    private String endHour;
    private String location;

    /** Creates an empty form; the controller fills it from the request parameters. */
    public SessionForm() {
    }

    /** Returns the connection id of the buddy the session is with, or null for a group session. */
    public String getBuddy() {
        return buddy;
    }

    /** Sets the connection id of the buddy the session is with. */
    public void setBuddy(String buddy) {
        this.buddy = buddy;
    }

    /** Returns the id of the group the session is for, or null for a buddy session. */
    public String getGroup() {
        return group;
    }

    /** Sets the id of the group the session is for. */
    public void setGroup(String group) {
        this.group = group;
    }

    /** Returns the chosen course code. */
    public String getCourseCode() {
        return courseCode;
    }

    /** Sets the chosen course code. */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /** Returns the chosen day as text, "0" for Monday up to "6" for Sunday. */
    public String getDay() {
        return day;
    }

    /** Sets the chosen day as text, "0" for Monday up to "6" for Sunday. */
    public void setDay(String day) {
        this.day = day;
    }

    /** Returns the start time as a clock hour in text, such as "19". */
    public String getStartHour() {
        return startHour;
    }

    /** Sets the start time as a clock hour in text, such as "19". */
    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    /** Returns the end time as a clock hour in text, such as "21". */
    public String getEndHour() {
        return endHour;
    }

    /** Sets the end time as a clock hour in text, such as "21". */
    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    /** Returns the meeting place as typed. */
    public String getLocation() {
        return location;
    }

    /** Sets the meeting place as typed. */
    public void setLocation(String location) {
        this.location = location;
    }
}
