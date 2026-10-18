package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.StudyGroup;

/** Stores and finds study groups. The rest of the app only ever sees this interface. */
public interface StudyGroupRepository {

    /** Saves a group, replacing any existing group with the same id. */
    void save(StudyGroup group);

    /** Returns the group with this id, or null if no such group. */
    StudyGroup findById(String id);

    /** Returns all groups, in the order they were saved. */
    List<StudyGroup> findAll();

    /** Returns an unused group id such as "G001" or "G002" */
    String nextId();

    /** Returns the groups led by this student, in the order they were saved. */
    List<StudyGroup> findByLeader(String studentId);

    /** Returns the groups this student belongs to (leader included), in the order they were saved. */
    List<StudyGroup> findByMember(String studentId);

    /** Returns the groups studying this course, in the order they were saved. */
    List<StudyGroup> findByCourse(String courseCode);
}
