package com.studybuddy.service;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;

/**
 * Hears about changes to who is in a study group, so other features (such as the Discord group
 * chat) can react without the group services knowing about them. The group services call every
 * listener after a change is saved; a listener must never throw.
 */
public interface GroupEventListener {

    /** Called after a group is created; its leader is already its first member. */
    void onGroupCreated(StudyGroup group);

    /** Called after a student is added to a group. */
    void onMemberJoined(StudyGroup group, Student student);

    /** Called after a student leaves or is removed from a group. */
    void onMemberLeft(StudyGroup group, Student student);

    /** Called after a group is closed. */
    void onGroupClosed(StudyGroup group);
}
