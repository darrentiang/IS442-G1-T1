package com.studybuddy.service;

import org.springframework.stereotype.Service;

import com.studybuddy.model.StudyGroup;

/** Works out what the navigation shows for the signed-in student: how many items wait for them. */
@Service
public class NavigationService {

    private final MatchRequestService matchRequestService;
    private final StudyGroupService groupService;
    private final MembershipService membershipService;

    /** Creates the service with the services it counts from (constructor injection). */
    public NavigationService(MatchRequestService matchRequestService, StudyGroupService groupService,
            MembershipService membershipService) {
        this.matchRequestService = matchRequestService;
        this.groupService = groupService;
        this.membershipService = membershipService;
    }

    /** Returns the pending buddy requests to this student and the pending join requests to the groups they lead. */
    public NavCounts countsFor(String studentId) {
        int joinRequests = 0;
        for (StudyGroup group : groupService.findLedBy(studentId)) {
            joinRequests += membershipService.findPendingForGroup(group.getId()).size();
        }
        return new NavCounts(matchRequestService.countPendingTo(studentId), joinRequests);
    }
}
