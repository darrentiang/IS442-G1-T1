package com.studybuddy.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.service.CourseService;
import com.studybuddy.service.JoinRequestService;
import com.studybuddy.service.StudyGroupService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** Serves group discovery, group details and the current student's join requests. */
@Controller
public class GroupBrowseController {

    private final JoinRequestService requestService;
    private final StudyGroupService groupService;
    private final UserService userService;
    private final CourseService courseService;

    /** Creates the controller with the services to which it delegates. */
    public GroupBrowseController(JoinRequestService requestService, StudyGroupService groupService,
            UserService userService, CourseService courseService) {
        this.requestService = requestService;
        this.groupService = groupService;
        this.userService = userService;
        this.courseService = courseService;
    }

    /** Shows public open groups with optional course and study goal filters. */
    @GetMapping("/groups")
    public String showGroups(@RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String goal, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        StudyGoal selectedGoal = null;
        Map<String, String> fieldErrors = new HashMap<>();
        if (goal != null && !goal.isBlank()) {
            try {
                selectedGoal = StudyGoal.valueOf(goal);
            } catch (IllegalArgumentException exception) {
                fieldErrors.put("goal", "Choose a valid study goal");
            }
        }
        String selectedCourse = courseCode == null || courseCode.isBlank() ? null : courseCode.trim();
        model.addAttribute("groups", fieldErrors.isEmpty()
                ? requestService.findBrowsable(selectedCourse, selectedGoal) : Collections.emptyList());
        model.addAttribute("fieldErrors", fieldErrors);
        model.addAttribute("goalValue", goal);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("goals", StudyGoal.values());
        model.addAttribute("courseCode", selectedCourse);
        model.addAttribute("selectedGoal", selectedGoal);
        model.addAttribute("student", userService.findStudent(userId));
        return "group/list";
    }

    /** Shows any group by direct link, including private and closed groups. */
    @GetMapping("/groups/{id}")
    public String showGroup(@PathVariable String id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        model.addAttribute("group", group);
        model.addAttribute("student", userService.findStudent(userId));
        model.addAttribute("leader", groupService.isLeader(group, userId));
        model.addAttribute("member", groupService.isMember(group, userId));
        model.addAttribute("full", groupService.isFull(group));
        model.addAttribute("pending", requestService.hasPendingRequest(userId, id));
        model.addAttribute("pendingStatus", RequestStatus.PENDING);
        model.addAttribute("days", new String[] {"Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"});
        return "group/detail";
    }

    /** Submits a student's join request and redirects with success or error feedback. */
    @PostMapping("/groups/{id}/join")
    public String requestToJoin(@PathVariable String id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        Student student = userService.findStudent(userId);
        try {
            requestService.requestToJoin(student, group);
        } catch (IllegalStateException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/" + id);
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Request sent to " + group.getName());
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/groups/" + id;
    }

    /** Shows every request submitted by the signed-in student. */
    @GetMapping("/groups/requests")
    public String showMyRequests(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students have join requests", "/groups");
        }
        model.addAttribute("requests", requestService.findByStudent(userId));
        return "group/my-requests";
    }

    private String errorRedirect(RedirectAttributes redirectAttributes, String message, String path) {
        redirectAttributes.addFlashAttribute("toastMessage", message);
        redirectAttributes.addFlashAttribute("toastType", "error");
        return "redirect:" + path;
    }
}
