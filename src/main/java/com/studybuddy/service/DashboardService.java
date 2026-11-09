package com.studybuddy.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Connection;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Gathers what the home pages show: the admin's overview numbers and what waits for a student. */
@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository groupRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final ConnectionRepository connectionRepository;

    /** Creates the service with the repositories it reads (constructor injection). */
    public DashboardService(StudentRepository studentRepository, StudyGroupRepository groupRepository,
            MatchRequestRepository matchRequestRepository, JoinRequestRepository joinRequestRepository,
            ConnectionRepository connectionRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.connectionRepository = connectionRepository;
    }

    /** Counts accounts, groups, waiting requests and connections for the admin's overview. */
    public AdminOverview adminOverview() {
        int students = 0;
        int suspended = 0;
        int withoutBuddy = 0;
        for (Student student : studentRepository.findAll()) {
            students++;
            if (student.getStatus() == AccountStatus.SUSPENDED) {
                suspended++;
            } else if (connectionRepository.findActiveFor(student.getId()).isEmpty()) {
                withoutBuddy++;
            }
        }
        int open = 0;
        int full = 0;
        int closed = 0;
        for (StudyGroup group : groupRepository.findAll()) {
            if (!group.isOpen()) {
                closed++;
            } else if (group.isFull()) {
                full++;
            } else {
                open++;
            }
        }
        int pendingBuddy = 0;
        for (MatchRequest request : matchRequestRepository.findAll()) {
            if (request.getStatus() == RequestStatus.PENDING) {
                pendingBuddy++;
            }
        }
        int pendingJoin = 0;
        for (JoinRequest request : joinRequestRepository.findAll()) {
            if (request.getStatus() == RequestStatus.PENDING) {
                pendingJoin++;
            }
        }
        int active = 0;
        for (Connection connection : connectionRepository.findAll()) {
            if (connection.isActive()) {
                active++;
            }
        }
        return new AdminOverview(students, suspended, withoutBuddy, open, full, closed, pendingBuddy,
                pendingJoin, active);
    }

    /** Returns each group this student leads that has join requests waiting, with how many; in group order. */
    public Map<StudyGroup, Integer> waitingJoinRequests(String studentId) {
        Map<StudyGroup, Integer> waiting = new LinkedHashMap<>();
        for (StudyGroup group : groupRepository.findByLeader(studentId)) {
            int count = 0;
            for (JoinRequest request : joinRequestRepository.findByGroup(group.getId())) {
                if (request.getStatus() == RequestStatus.PENDING) {
                    count++;
                }
            }
            if (count > 0) {
                waiting.put(group, count);
            }
        }
        return waiting;
    }
}
