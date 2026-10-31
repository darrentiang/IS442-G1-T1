package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.Visibility;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Finds discoverable groups and manages students' requests to join them. */
@Service
public class JoinRequestService {

    private final JoinRequestRepository requestRepository;
    private final StudyGroupRepository groupRepository;
    private final StudyGroupService groupService;

    /** Creates the service with its storage contracts and shared membership rules. */
    public JoinRequestService(JoinRequestRepository requestRepository,
            StudyGroupRepository groupRepository, StudyGroupService groupService) {
        this.requestRepository = requestRepository;
        this.groupRepository = groupRepository;
        this.groupService = groupService;
    }

    /** Validates eligibility and saves a new pending request, including after a decline. */
    public JoinRequest requestToJoin(Student student, StudyGroup group) {
        if (student == null) {
            throw new IllegalStateException("Only students can request to join a group");
        }
        if (group == null) {
            throw new IllegalStateException("This group could not be found");
        }
        if (!group.isOpen()) {
            throw new IllegalStateException("This group is closed");
        }
        if (groupService.isFull(group)) {
            throw new IllegalStateException("This group is full");
        }
        if (groupService.isMember(group, student.getId())) {
            throw new IllegalStateException("You're already in this group");
        }
        if (hasPendingRequest(student.getId(), group.getId())) {
            throw new IllegalStateException("You already asked to join");
        }
        JoinRequest request = new JoinRequest(requestRepository.nextId(), student, group);
        requestRepository.save(request);
        return request;
    }

    /** Returns all of a student's requests in ascending numeric request ID order. */
    public List<JoinRequest> findByStudent(String studentId) {
        List<JoinRequest> ordered = new ArrayList<>();
        for (JoinRequest request : requestRepository.findByStudent(studentId)) {
            int position = 0;
            int number = Integer.parseInt(request.getId().substring(1));
            while (position < ordered.size()
                    && Integer.parseInt(ordered.get(position).getId().substring(1)) < number) {
                position++;
            }
            ordered.add(position, request);
        }
        return ordered;
    }

    /** Returns whether the student has a pending request for the specified group. */
    public boolean hasPendingRequest(String studentId, String groupId) {
        for (JoinRequest request : requestRepository.findByStudent(studentId)) {
            if (request.getGroup().getId().equals(groupId)
                    && request.getStatus() == RequestStatus.PENDING) {
                return true;
            }
        }
        return false;
    }

    /** Returns public open groups matching every supplied course and goal filter. */
    public List<StudyGroup> findBrowsable(String courseCode, StudyGoal goal) {
        List<StudyGroup> candidates;
        if (courseCode == null) {
            candidates = groupRepository.findAll();
        } else {
            candidates = groupRepository.findByCourse(courseCode);
        }
        List<StudyGroup> result = new ArrayList<>();
        for (StudyGroup group : candidates) {
            if (group.getVisibility() == Visibility.PUBLIC && group.isOpen()
                    && (goal == null || group.getGoals().contains(goal))) {
                result.add(group);
            }
        }
        return result;
    }
}
