package com.studybuddy.repository;

import java.util.List;

import com.studybuddy.model.StudyGroup;

/** Stores and finds study groups. The rest of the app only ever sees this interface. */
public interface StudyGroupRepository {

    /** Saves a group, replacing any existing group with the same id. */
    void save(StudyGroup group);

    /** Returns the group with this id, or null if no such group. */
    StudyGroup findById(String id);

    /** Returns all groups. */
    List<StudyGroup> findAll();
}
