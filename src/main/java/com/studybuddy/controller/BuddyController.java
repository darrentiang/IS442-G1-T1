package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.RequestNotAllowedException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.service.BuddySearch;
import com.studybuddy.service.CourseService;
import com.studybuddy.service.MatchRequestService;
import com.studybuddy.service.MatchingService;
import com.studybuddy.service.ProfileService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** Find Buddies: ranked matches, pass and undo, sending requests, and a candidate's profile. */
@Controller
public class BuddyController {

    private static final int PERCENT = 100;
    private static final String NOT_A_STUDENT = "Find buddies is for students";

    private final MatchingService matchingService;
    private final MatchRequestService requestService;
    private final ProfileService profileService;
    private final UserService userService;
    private final CourseService courseService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public BuddyController(MatchingService matchingService, MatchRequestService requestService,
            ProfileService profileService, UserService userService, CourseService courseService) {
        this.matchingService = matchingService;
        this.requestService = requestService;
        this.profileService = profileService;
        this.userService = userService;
        this.courseService = courseService;
    }

    /** Shows ranked matches; with no course and no goal, searches the student's own target course. */
    @GetMapping("/buddies")
    public String showBuddies(@RequestParam(required = false) String course,
            @RequestParam(required = false) String goal, @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int minHours, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        BuddySearch search = buildSearch(course, goal, mode, minHours);
        matchingService.applyDefaults(student, search);
        model.addAttribute("results", matchingService.findBuddies(student, search));
        model.addAttribute("search", search);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("goals", StudyGoal.values());
        model.addAttribute("strategyLabel", matchingService.getStrategyLabel());
        model.addAttribute("criterionLabels", matchingService.getCriterionLabels());
        model.addAttribute("minimumPercent", Math.round(matchingService.getConfig().getMinimumScore() * PERCENT));
        return "student/find-buddies";
    }

    /** Sends a buddy request with an optional message, then returns to the same search. */
    @PostMapping("/buddies/{id}/request")
    public String sendRequest(@PathVariable String id, @RequestParam(required = false) String message,
            @RequestParam(required = false) String course, @RequestParam(required = false) String goal,
            @RequestParam(required = false) String mode, @RequestParam(defaultValue = "0") int minHours,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        try {
            Student to = requestService.sendRequest(student, id, message).getTo();
            Toasts.success(redirectAttributes, "Request sent to " + to.getName());
        } catch (RequestNotAllowedException exception) {
            Toasts.error(redirectAttributes, exception.getMessage());
        }
        return redirectToSearch(redirectAttributes, course, goal, mode, minHours);
    }

    /** Hides a candidate from future results and offers an Undo banner. */
    @PostMapping("/buddies/{id}/pass")
    public String pass(@PathVariable String id, @RequestParam(required = false) String course,
            @RequestParam(required = false) String goal, @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int minHours, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        try {
            matchingService.pass(student, id);
            redirectAttributes.addFlashAttribute("lastPassedId", id);
            redirectAttributes.addFlashAttribute("lastPassedName", userService.findStudent(id).getName());
        } catch (BusinessRuleException exception) {
            Toasts.error(redirectAttributes, exception.getMessage());
        }
        return redirectToSearch(redirectAttributes, course, goal, mode, minHours);
    }

    /** Puts a passed-on candidate back in the results. */
    @PostMapping("/buddies/{id}/undo-pass")
    public String undoPass(@PathVariable String id, @RequestParam(required = false) String course,
            @RequestParam(required = false) String goal, @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int minHours, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        Student candidate = userService.findStudent(id);
        if (candidate != null && matchingService.undoPass(student, id)) {
            Toasts.success(redirectAttributes, candidate.getName() + " is back in your results");
        } else {
            Toasts.error(redirectAttributes, "There was nothing to undo");
        }
        return redirectToSearch(redirectAttributes, course, goal, mode, minHours);
    }

    /** Shows a candidate's profile: public view (contact hidden unless connected), match summary and overlap grid. */
    @GetMapping("/buddies/{id}")
    public String showCandidate(@PathVariable String id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        Student candidate = userService.findStudent(id);
        if (candidate == null || candidate.getId().equals(student.getId())
                || candidate.getStatus() != AccountStatus.ACTIVE || candidate.getProfile() == null) {
            Toasts.error(redirectAttributes, "This student could not be found");
            return "redirect:/buddies";
        }
        AvailabilityGrid ownGrid = (student.getProfile() == null) ? null : student.getProfile().getAvailability();
        model.addAttribute("candidate", candidate);
        model.addAttribute("profile", profileService.viewProfile(student, candidate));
        model.addAttribute("result", matchingService.evaluate(student, candidate));
        model.addAttribute("relationship", profileService.relationship(student, candidate));
        model.addAttribute("grid", new OverlapGridView(ownGrid, candidate.getProfile().getAvailability()));
        model.addAttribute("criterionLabels", matchingService.getCriterionLabels());
        return "student/candidate";
    }

    /** Collects the search bar values into one form object. */
    private BuddySearch buildSearch(String course, String goal, String mode, int minHours) {
        BuddySearch search = new BuddySearch();
        search.setCourseCode(course);
        search.setGoal(goal);
        search.setMode(mode);
        search.setMinHours(minHours);
        return search;
    }

    /** Redirects to /buddies with the same search, leaving out blank values. */
    private String redirectToSearch(RedirectAttributes redirectAttributes, String course, String goal,
            String mode, int minHours) {
        addIfPresent(redirectAttributes, "course", course);
        addIfPresent(redirectAttributes, "goal", goal);
        addIfPresent(redirectAttributes, "mode", mode);
        if (minHours > 0) {
            redirectAttributes.addAttribute("minHours", minHours);
        }
        return "redirect:/buddies";
    }

    /** Adds a query parameter to the redirect unless the value is blank. */
    private void addIfPresent(RedirectAttributes redirectAttributes, String name, String value) {
        if (value != null && !value.isBlank()) {
            redirectAttributes.addAttribute(name, value);
        }
    }
}
