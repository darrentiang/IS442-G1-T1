package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.Student;
import com.studybuddy.service.MatchRequestService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** Buddy requests a student has received: the inbox, accept and decline. */
@Controller
public class ConnectionController {

    private static final String NOT_A_STUDENT = "Buddy requests are for students";

    private final MatchRequestService requestService;
    private final UserService userService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public ConnectionController(MatchRequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    /** Shows the pending buddy requests sent to the signed-in student. */
    @GetMapping("/requests")
    public String showRequests(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        model.addAttribute("requests", requestService.findPendingTo(student.getId()));
        return "student/requests";
    }

    /** Accepts a request and connects the two students; returns to the inbox with a toast. */
    @PostMapping("/requests/{id}/accept")
    public String acceptRequest(@PathVariable String id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        try {
            MatchRequest request = requestService.accept(id, student.getId());
            Toasts.success(redirectAttributes, "You're now connected with " + request.getFrom().getName());
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:/requests";
    }

    /** Declines a request; returns to the inbox with a toast. */
    @PostMapping("/requests/{id}/decline")
    public String declineRequest(@PathVariable String id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        try {
            MatchRequest request = requestService.decline(id, student.getId());
            Toasts.success(redirectAttributes, "Request from " + request.getFrom().getName() + " declined");
        } catch (BusinessRuleException e) {
            Toasts.error(redirectAttributes, e.getMessage());
        }
        return "redirect:/requests";
    }
}
