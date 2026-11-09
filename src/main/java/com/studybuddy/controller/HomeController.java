package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studybuddy.model.Student;
import com.studybuddy.model.User;
import com.studybuddy.service.ConnectionService;
import com.studybuddy.service.DashboardService;
import com.studybuddy.service.MatchRequestService;
import com.studybuddy.service.MatchingConfigService;
import com.studybuddy.service.ProfileEditService;
import com.studybuddy.service.StudyGroupService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * The signed-in user's home page. A student sees what waits for them (buddy requests, join
 * requests to groups they lead), their groups and their buddies; an administrator sees an
 * overview of accounts, groups and requests with shortcuts to their pages.
 */
@Controller
public class HomeController {

    private final UserService userService;
    private final ProfileEditService editService;
    private final MatchRequestService matchRequestService;
    private final StudyGroupService groupService;
    private final ConnectionService connectionService;
    private final DashboardService dashboardService;
    private final MatchingConfigService matchingConfigService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public HomeController(UserService userService, ProfileEditService editService,
            MatchRequestService matchRequestService, StudyGroupService groupService,
            ConnectionService connectionService, DashboardService dashboardService,
            MatchingConfigService matchingConfigService) {
        this.userService = userService;
        this.editService = editService;
        this.matchRequestService = matchRequestService;
        this.groupService = groupService;
        this.connectionService = connectionService;
        this.dashboardService = dashboardService;
        this.matchingConfigService = matchingConfigService;
    }

    /** Shows the student's home or the administrator's overview, depending on who is signed in. */
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        String userId = CurrentUser.getId(session);
        User user = userService.findUser(userId);
        Student student = userService.findStudent(userId);                // null for an admin
        model.addAttribute("user", user);
        if (student == null) {
            model.addAttribute("overview", dashboardService.adminOverview());
            model.addAttribute("strategyLabel", matchingConfigService.getStrategyLabels()
                    .get(matchingConfigService.getConfig().getActiveStrategyName()));
            return "admin/overview";
        }
        model.addAttribute("student", student);
        model.addAttribute("needsAvailability", editService.needsAvailability(student));
        model.addAttribute("buddyRequests", matchRequestService.findPendingTo(userId));
        model.addAttribute("joinRequests", dashboardService.waitingJoinRequests(userId));
        model.addAttribute("ledGroups", groupService.findLedBy(userId));
        model.addAttribute("joinedGroups", groupService.findJoinedBy(userId));
        model.addAttribute("buddies", connectionService.findConnectionsFor(userId));
        return "shared/home";
    }
}
