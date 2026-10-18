package com.studybuddy.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;

/** LinkedHashMap-backed implementation; keeps insertion order, so lists come back in seed order. */
@Repository
public class InMemoryStudyGroupRepository implements StudyGroupRepository {

    private final Map<String, StudyGroup> store = new LinkedHashMap<>();
    private int nextNumber = 0;

    /** Saves a group, replacing any existing group with the same id. */
    @Override
    public void save(StudyGroup group) {
        store.put(group.getId(), group);
    }

    /** Returns the group with this id, or null if no such group. */
    @Override
    public StudyGroup findById(String id) {
        return store.get(id);
    }

    /** Returns all groups, in the order they were saved. */
    @Override
    public List<StudyGroup> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Returns an unused group id such as "G001", counting up and skipping ids already in the store. */
    @Override
    public String nextId() {
        String id;
        do {
            nextNumber++;
            id = String.format("G%03d", nextNumber);
        } while (store.containsKey(id));
        return id;
    }

    /** Returns the groups led by this student, in the order they were saved. */
    @Override
    public List<StudyGroup> findByLeader(String studentId) {
        List<StudyGroup> result = new ArrayList<>();
        for (StudyGroup group : store.values()) {
            if (group.getLeader() != null && group.getLeader().getId().equals(studentId)) {
                result.add(group);
            }
        }
        return result;
    }

    /** Returns the groups this student belongs to (leader included), in the order they were saved. */
    @Override
    public List<StudyGroup> findByMember(String studentId) {
        List<StudyGroup> result = new ArrayList<>();
        for (StudyGroup group : store.values()) {
            for (Student member : group.getMembers()) {
                if (member.getId().equals(studentId)) {
                    result.add(group);
                    break;
                }
            }
        }
        return result;
    }

    /** Returns the groups studying this course, in the order they were saved. */
    @Override
    public List<StudyGroup> findByCourse(String courseCode) {
        List<StudyGroup> result = new ArrayList<>();
        for (StudyGroup group : store.values()) {
            if (group.getCourse() != null && group.getCourse().getCode().equals(courseCode)) {
                result.add(group);
            }
        }
        return result;
    }
}
