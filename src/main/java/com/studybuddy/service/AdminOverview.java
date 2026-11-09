package com.studybuddy.service;

/** The numbers on the administrator's overview page: accounts, groups and what is waiting. */
public class AdminOverview {

    private final int students;
    private final int suspendedStudents;
    private final int studentsWithoutBuddy;
    private final int openGroups;
    private final int fullGroups;
    private final int closedGroups;
    private final int pendingBuddyRequests;
    private final int pendingJoinRequests;
    private final int activeConnections;

    /**
     * Creates the overview.
     *
     * @param students every student account
     * @param suspendedStudents student accounts that are suspended
     * @param studentsWithoutBuddy active students with no active buddy connection
     * @param openGroups groups open to join with places left
     * @param fullGroups open groups with no places left
     * @param closedGroups groups their leader closed
     * @param pendingBuddyRequests buddy requests no one has answered yet
     * @param pendingJoinRequests group join requests no leader has answered yet
     * @param activeConnections buddy connections still active
     */
    public AdminOverview(int students, int suspendedStudents, int studentsWithoutBuddy, int openGroups,
            int fullGroups, int closedGroups, int pendingBuddyRequests, int pendingJoinRequests,
            int activeConnections) {
        this.students = students;
        this.suspendedStudents = suspendedStudents;
        this.studentsWithoutBuddy = studentsWithoutBuddy;
        this.openGroups = openGroups;
        this.fullGroups = fullGroups;
        this.closedGroups = closedGroups;
        this.pendingBuddyRequests = pendingBuddyRequests;
        this.pendingJoinRequests = pendingJoinRequests;
        this.activeConnections = activeConnections;
    }

    /** Returns every student account. */
    public int getStudents() {
        return students;
    }

    /** Returns the student accounts that are suspended. */
    public int getSuspendedStudents() {
        return suspendedStudents;
    }

    /** Returns the active students with no active buddy connection. */
    public int getStudentsWithoutBuddy() {
        return studentsWithoutBuddy;
    }

    /** Returns the groups open to join with places left. */
    public int getOpenGroups() {
        return openGroups;
    }

    /** Returns the open groups with no places left. */
    public int getFullGroups() {
        return fullGroups;
    }

    /** Returns the groups their leader closed. */
    public int getClosedGroups() {
        return closedGroups;
    }

    /** Returns the buddy requests no one has answered yet. */
    public int getPendingBuddyRequests() {
        return pendingBuddyRequests;
    }

    /** Returns the group join requests no leader has answered yet. */
    public int getPendingJoinRequests() {
        return pendingJoinRequests;
    }

    /** Returns the buddy connections still active. */
    public int getActiveConnections() {
        return activeConnections;
    }
}
