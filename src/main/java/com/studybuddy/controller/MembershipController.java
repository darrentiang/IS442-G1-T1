package com.studybuddy.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.service.MembershipService;
import com.studybuddy.service.StudyGroupService;

import jakarta.servlet.http.HttpSession;

/** The leader's manage page for one group: answering join requests and removing members. */
@Controller
public class MembershipController {

    private final StudyGroupService groupService;
    private final MembershipService membershipService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public MembershipController(StudyGroupService groupService, MembershipService membershipService) {
        this.groupService = groupService;
        this.membershipService = membershipService;
    }

    /** Shows the group's pending requests and members; only the leader may see it. */
    @GetMapping("/groups/{id}/manage")
    public String showManagePage(@PathVariable String id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        String notLeader = redirectIfNotLeader(id, userId, redirectAttributes);
        if (notLeader != null) {
            return notLeader;
        }
        StudyGroup group = groupService.findById(id);
        List<JoinRequest> pendingRequests = membershipService.findPendingForGroup(id);
        Set<String> takingCourse = new HashSet<>();     // ids of requests whose student takes the group's course
        for (JoinRequest request : pendingRequests) {
            if (membershipService.takesGroupCourse(group, request.getStudent())) {
                takingCourse.add(request.getId());
            }
        }
        model.addAttribute("group", group);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("takingCourse", takingCourse);
        return "group/manage";               // -> templates/group/manage.html
    }

    /** Accepts a join request, then returns to the manage page with a toast. */
    @PostMapping("/groups/{id}/requests/{requestId}/accept")
    public String acceptRequest(@PathVariable String id, @PathVariable String requestId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        String notLeader = redirectIfNotLeader(id, userId, redirectAttributes);
        if (notLeader != null) {
            return notLeader;
        }
        try {
            Student student = membershipService.acceptRequest(id, requestId, userId);
            Toasts.success(redirectAttributes, student.getName() + " added to the group");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:/groups/" + id + "/manage";
    }

    /** Declines a join request, then returns to the manage page with a toast. */
    @PostMapping("/groups/{id}/requests/{requestId}/reject")
    public String rejectRequest(@PathVariable String id, @PathVariable String requestId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        String notLeader = redirectIfNotLeader(id, userId, redirectAttributes);
        if (notLeader != null) {
            return notLeader;
        }
        try {
            Student student = membershipService.rejectRequest(id, requestId, userId);
            Toasts.success(redirectAttributes, "Request from " + student.getName() + " declined");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:/groups/" + id + "/manage";
    }

    /** Removes a member from the group, then returns to the manage page with a toast. */
    @PostMapping("/groups/{id}/members/{studentId}/remove")
    public String removeMember(@PathVariable String id, @PathVariable String studentId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        String notLeader = redirectIfNotLeader(id, userId, redirectAttributes);
        if (notLeader != null) {
            return notLeader;
        }
        try {
            Student student = membershipService.removeMember(id, studentId, userId);
            Toasts.success(redirectAttributes, student.getName() + " removed from the group");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:/groups/" + id + "/manage";
    }

    /**
     * Returns where to send a visitor who may not manage this group (an unknown group goes to the
     * group list, a non-leader to the group's page), with an error toast; null if they are its leader.
     */
    private String redirectIfNotLeader(String groupId, String userId, RedirectAttributes redirectAttributes) {
        StudyGroup group = groupService.findById(groupId);
        if (group == null) {
            Toasts.error(redirectAttributes, "That group no longer exists");
            return "redirect:/groups";
        }
        if (!groupService.isLeader(group, userId)) {
            Toasts.error(redirectAttributes, "Only the group leader can manage members");
            return "redirect:/groups/" + groupId;
        }
        return null;
    }
}
