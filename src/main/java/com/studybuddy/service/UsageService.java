package com.studybuddy.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudyGroupRepository;

/** Counts a student's activity across the app, for the admin's view of account usage. */
@Service
public class UsageService {

    private final MatchRequestRepository matchRequestRepository;
    private final ConnectionRepository connectionRepository;
    private final StudyGroupRepository groupRepository;
    private final JoinRequestRepository joinRequestRepository;

    /** Creates the service with the repositories it reads from (constructor injection). */
    public UsageService(MatchRequestRepository matchRequestRepository, ConnectionRepository connectionRepository,
            StudyGroupRepository groupRepository, JoinRequestRepository joinRequestRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.connectionRepository = connectionRepository;
        this.groupRepository = groupRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    /** Returns this student's activity counts, label to count, in display order. */
    public Map<String, Integer> getUsage(String studentId) {
        Map<String, Integer> usage = new LinkedHashMap<>();
        usage.put("Buddy requests sent", matchRequestRepository.findFrom(studentId).size());
        usage.put("Buddy requests received", matchRequestRepository.findTo(studentId).size());
        usage.put("Active connections", connectionRepository.findActiveFor(studentId).size());
        usage.put("Groups joined", groupRepository.findByMember(studentId).size());
        usage.put("Groups led", groupRepository.findByLeader(studentId).size());
        usage.put("Group join requests", joinRequestRepository.findByStudent(studentId).size());
        return usage;
    }
}
