package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Course;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;

/**
 * Business rules for who is in a group: answering join requests and removing members.
 * Every failure throws IllegalStateException with a user-readable message.
 */
@Service
public class MembershipService {

    private final StudyGroupRepository groupRepository;
    private final JoinRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final StudyGroupService groupService;

    /** Creates the service with the repositories and services it uses (constructor injection). */
    public MembershipService(StudyGroupRepository groupRepository,
                             JoinRequestRepository requestRepository,
                             StudentRepository studentRepository,
                             StudyGroupService groupService) {
        this.groupRepository = groupRepository;
        this.requestRepository = requestRepository;
        this.studentRepository = studentRepository;
        this.groupService = groupService;
    }

    /** Returns the PENDING requests made to this group, in the order they were saved. */
    public List<JoinRequest> findPendingForGroup(String groupId) {
        List<JoinRequest> pending = new ArrayList<>();
        for (JoinRequest request : requestRepository.findByGroup(groupId)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                pending.add(request);
            }
        }
        return pending;
    }

    /** Returns true if the student's current courses include the group's course. */
    public boolean takesGroupCourse(StudyGroup group, Student student) {
        if (student.getProfile() == null || student.getProfile().getCoursesTaken() == null) {
            return false;
        }
        for (Course course : student.getProfile().getCoursesTaken()) {
            if (course.getCode().equals(group.getCourse().getCode())) {
                return true;
            }
        }
        return false;
    }

    /** Accepts a PENDING request from an active student to an open, not-full group led by this leader; returns the new member. */
    public Student acceptRequest(String groupId, String requestId, String leaderId) {
        StudyGroup group = findGroupLedBy(groupId, leaderId);
        JoinRequest request = findPendingRequest(group, requestId);
        if (!group.isOpen()) {
            throw new IllegalStateException("This group is closed");
        }
        if (request.getStudent().getStatus() == AccountStatus.SUSPENDED) {
            throw new IllegalStateException(request.getStudent().getName() + "'s account is suspended");
        }
        // addMember goes first: it throws (full, already a member) before changing anything, and
        // accept() cannot fail on a PENDING request. The repositories hold these same objects, so
        // a half-finished change (ACCEPTED but not joined) would stick even without save().
        addMember(group, request.getStudent());
        request.accept();
        requestRepository.save(request);
        return request.getStudent();
    }

    /** Declines a PENDING request to a group led by this leader; returns the student who asked. */
    public Student rejectRequest(String groupId, String requestId, String leaderId) {
        StudyGroup group = findGroupLedBy(groupId, leaderId);
        JoinRequest request = findPendingRequest(group, requestId);
        request.decline();
        requestRepository.save(request);
        return request.getStudent();
    }

    /** Removes a student from a group; only the leader may do this, and the leader cannot remove themselves. */
    public Student removeMember(String groupId, String studentId, String leaderId) {
        StudyGroup group = findGroupLedBy(groupId, leaderId);
        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throw new IllegalStateException("That student does not exist");
        }
        group.removeMember(student);
        groupRepository.save(group);
        return student;
    }

    /** Adds the student to the group and saves it; throws GroupFullException if the group is full. */
    private void addMember(StudyGroup group, Student student) {
        group.addMember(student);
        groupRepository.save(group);
    }

    /** Returns the group, or throws if it does not exist or this student does not lead it. */
    private StudyGroup findGroupLedBy(String groupId, String leaderId) {
        StudyGroup group = groupRepository.findById(groupId);
        if (group == null) {
            throw new IllegalStateException("That group no longer exists");
        }
        if (!groupService.isLeader(group, leaderId)) {
            throw new IllegalStateException("Only the group leader can manage members");
        }
        return group;
    }

    /** Returns the request, or throws if it does not exist, is for another group, or is not PENDING. */
    private JoinRequest findPendingRequest(StudyGroup group, String requestId) {
        JoinRequest request = requestRepository.findById(requestId);
        if (request == null || !request.getGroup().getId().equals(group.getId())) {
            throw new IllegalStateException("That request does not belong to this group");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request was already handled");
        }
        return request;
    }
}
