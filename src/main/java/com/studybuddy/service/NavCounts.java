package com.studybuddy.service;

/** The pending items shown as counts in the navigation, for one signed-in student. */
public class NavCounts {

    private final int buddyRequests;
    private final int groupJoinRequests;

    /**
     * Creates the counts.
     *
     * @param buddyRequests buddy requests waiting for this student's answer
     * @param groupJoinRequests join requests waiting in groups this student leads
     */
    public NavCounts(int buddyRequests, int groupJoinRequests) {
        this.buddyRequests = buddyRequests;
        this.groupJoinRequests = groupJoinRequests;
    }

    /** Returns the buddy requests waiting for this student's answer. */
    public int getBuddyRequests() {
        return buddyRequests;
    }

    /** Returns the join requests waiting in groups this student leads. */
    public int getGroupJoinRequests() {
        return groupJoinRequests;
    }
}
