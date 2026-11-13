package com.studybuddy.controller;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudySession;
import com.studybuddy.service.ActivityFeedService;
import com.studybuddy.service.SessionForm;
import com.studybuddy.service.SessionService;
import com.studybuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Study sessions: my sessions, planning one with a buddy or group, replying, confirming, and a group's sessions and activity. */
@Controller
public class SessionController {

    private static final String NOT_A_STUDENT = "Study sessions are for students";
    private static final String BACK_TO_GROUP = "group";

    private final SessionService sessionService;
    private final ActivityFeedService feedService;
    private final UserService userService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public SessionController(SessionService sessionService, ActivityFeedService feedService, UserService userService) {
        this.sessionService = sessionService;
        this.feedService = feedService;
        this.userService = userService;
    }

    /** Shows the signed-in student's upcoming and past sessions. */
    @GetMapping("/sessions")
    public String showMySessions(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        model.addAttribute("upcoming", sessionService.upcomingFor(student.getId()));
        model.addAttribute("past", sessionService.pastFor(student.getId()));
        return "group/sessions";
    }

    /** Shows the form to plan a session with a buddy or a group; day and times may arrive pre-filled from a suggestion. */
    @GetMapping("/sessions/new")
    public String showPlanForm(@RequestParam(required = false) String buddy,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String startHour,
            @RequestParam(required = false) String endHour,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        SessionForm form = buildForm(buddy, group, courseCode, day, startHour, endHour, null);
        try {
            addFormModel(form, student, model);
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
            return "redirect:/sessions";
        }
        return "group/session-form";
    }

    /** Proposes a session; field errors re-show the form, success goes to the session list with a toast. */
    @PostMapping("/sessions")
    public String proposeSession(@RequestParam(required = false) String buddy,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String startHour,
            @RequestParam(required = false) String endHour,
            @RequestParam(required = false) String location,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        SessionForm form = buildForm(buddy, group, courseCode, day, startHour, endHour, location);
        try {
            Map<String, String> fieldErrors = sessionService.validate(form, student);
            if (!fieldErrors.isEmpty()) {
                addFormModel(form, student, model);
                model.addAttribute("fieldErrors", fieldErrors);
                return "group/session-form";
            }
            StudySession created = sessionService.propose(form, student);
            Toasts.success(redirectAttributes, "Session proposed for " + sessionService.whenOf(created)
                    + ". Everyone invited can now reply.");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
            return "redirect:/sessions";
        }
        return isBlank(group) ? "redirect:/sessions" : "redirect:/groups/" + group + "/sessions";
    }

    /** Records the signed-in student's answer, Going or Can't. */
    @PostMapping("/sessions/{id}/answer")
    public String answer(@PathVariable String id, @RequestParam(required = false) String answer,
            @RequestParam(required = false) String back, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        String target = backPath(id, back);
        try {
            sessionService.answer(id, student.getId(), answer);
            Toasts.success(redirectAttributes, "GOING".equals(answer) ? "You're going" : "Marked as can't make it");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:" + target;
    }

    /** Confirms a proposed session (its proposer only). */
    @PostMapping("/sessions/{id}/confirm")
    public String confirm(@PathVariable String id, @RequestParam(required = false) String back,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        String target = backPath(id, back);
        try {
            StudySession confirmed = sessionService.confirm(id, student.getId());
            Toasts.success(redirectAttributes, "Session confirmed for " + sessionService.whenOf(confirmed));
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:" + target;
    }

    /** Marks a confirmed session as done (its proposer only). */
    @PostMapping("/sessions/{id}/done")
    public String markDone(@PathVariable String id, @RequestParam(required = false) String back,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        String target = backPath(id, back);
        try {
            sessionService.markDone(id, student.getId());
            Toasts.success(redirectAttributes, "Session marked as done");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:" + target;
    }

    /** Cancels a session that hasn't happened yet (its proposer only). */
    @PostMapping("/sessions/{id}/cancel")
    public String cancel(@PathVariable String id, @RequestParam(required = false) String back,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        String target = backPath(id, back);
        try {
            sessionService.cancel(id, student.getId());
            Toasts.success(redirectAttributes, "Session cancelled");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:" + target;
    }

    /** Shows a group's sessions and activity feed to its members. */
    @GetMapping("/groups/{id}/sessions")
    public String showGroupSessions(@PathVariable String id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/groups/" + id;
        }
        StudyGroup group;
        try {
            group = sessionService.memberGroup(id, student.getId());
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
            return "redirect:/groups/" + id;
        }
        model.addAttribute("group", group);
        model.addAttribute("upcoming", sessionService.forGroup(id, student.getId(), true));
        model.addAttribute("past", sessionService.forGroup(id, student.getId(), false));
        model.addAttribute("feed", feedService.recentFor(id));
        return "group/group-sessions";
    }

    /** Puts what the plan form shows into the model: who it's with, course choices, suggested times and time choices. */
    private void addFormModel(SessionForm form, Student student, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("targetLabel", sessionService.targetLabel(form, student));
        model.addAttribute("courseOptions", sessionService.courseOptions(form, student));
        model.addAttribute("suggestions", sessionService.suggestTimes(sessionService.participantsFor(form, student)));
        model.addAttribute("minHours", sessionService.minSessionHours());
        List<String> dayNames = new ArrayList<>();
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            dayNames.add(AvailabilityGrid.dayName(day));
        }
        model.addAttribute("dayNames", dayNames);
        List<Integer> startHours = new ArrayList<>();
        for (int hour = AvailabilityGrid.FIRST_HOUR; hour < AvailabilityGrid.LAST_HOUR; hour++) {
            startHours.add(hour);
        }
        List<Integer> endHours = new ArrayList<>();
        for (int hour = AvailabilityGrid.FIRST_HOUR + 1; hour <= AvailabilityGrid.LAST_HOUR; hour++) {
            endHours.add(hour);
        }
        model.addAttribute("startHours", startHours);
        model.addAttribute("endHours", endHours);
    }

    /** Collects the form values into one object. */
    private SessionForm buildForm(String buddy, String group, String courseCode, String day,
            String startHour, String endHour, String location) {
        SessionForm form = new SessionForm();
        form.setBuddy(buddy);
        form.setGroup(group);
        form.setCourseCode(courseCode);
        form.setDay(day);
        form.setStartHour(startHour);
        form.setEndHour(endHour);
        form.setLocation(location);
        return form;
    }

    /** Returns where to go after an action: the group's sessions page when asked and the session has a group, otherwise My sessions. */
    private String backPath(String sessionId, String back) {
        String groupId = sessionService.groupIdOf(sessionId);
        if (BACK_TO_GROUP.equals(back) && groupId != null) {
            return "/groups/" + groupId + "/sessions";
        }
        return "/sessions";
    }

    /** Returns true for null or blank text. */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
