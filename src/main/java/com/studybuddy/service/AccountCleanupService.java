package com.studybuddy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.Connection;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Detaches a student from everything that points at them, so their account can be deleted cleanly. */
@Service
public class AccountCleanupService {

    private final ConnectionRepository connectionRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final StudyGroupRepository groupRepository;
    private final StudyGroupService groupService;

    /** Creates the service with the repositories it updates and the group service it leaves groups through (constructor injection). */
    public AccountCleanupService(ConnectionRepository connectionRepository,
            MatchRequestRepository matchRequestRepository, JoinRequestRepository joinRequestRepository,
            StudyGroupRepository groupRepository, StudyGroupService groupService) {
        this.connectionRepository = connectionRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.groupRepository = groupRepository;
        this.groupService = groupService;
    }

    /** Returns the groups this student leads, so the admin can see who takes over before deleting. */
    public List<StudyGroup> findLedGroups(String studentId) {
        return groupRepository.findByLeader(studentId);
    }

    /**
     * Ends the student's connections, declines their pending buddy and join requests, and takes
     * them out of every group they are in, handing over leadership the same way leaving does.
     */
    public void detach(Student student) {
        String id = student.getId();
        for (Connection connection : connectionRepository.findActiveFor(id)) {
            connection.setActive(false);
            connectionRepository.save(connection);
        }
        for (MatchRequest request : matchRequestRepository.findFrom(id)) {
            declineIfPending(request);
        }
        for (MatchRequest request : matchRequestRepository.findTo(id)) {
            declineIfPending(request);
        }
        for (JoinRequest request : joinRequestRepository.findByStudent(id)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                request.decline();
                joinRequestRepository.save(request);
            }
        }
        for (StudyGroup group : groupRepository.findByMember(id)) {
            groupService.leaveGroup(group.getId(), id);
        }
    }

    /** Declines a buddy request if it is still waiting for an answer. */
    private void declineIfPending(MatchRequest request) {
        if (request.getStatus() == RequestStatus.PENDING) {
            request.decline();
            matchRequestRepository.save(request);
        }
    }
}
