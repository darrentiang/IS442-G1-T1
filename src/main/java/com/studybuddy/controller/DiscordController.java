package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.service.DiscordService;
import com.studybuddy.service.StudyGroupService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** The group chat on Discord: the leader sets up a group's channel, and members connect their Discord account. */
@Controller
public class DiscordController {

    private final DiscordService discordService;
    private final StudyGroupService groupService;
    private final UserService userService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public DiscordController(DiscordService discordService, StudyGroupService groupService, UserService userService) {
        this.discordService = discordService;
        this.groupService = groupService;
        this.userService = userService;
    }

    /** Creates the group's Discord channel; leader only. Returns to the group with a toast. */
    @PostMapping("/groups/{id}/discord/setup")
    public String setUpChannel(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            return errorRedirect(redirectAttributes, "Only students can manage groups", "/home");
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        try {
            discordService.setUpChannel(group, student.getId());
        } catch (BusinessRuleException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/" + id);
        }
        Toasts.success(redirectAttributes, "Discord channel set up for " + group.getName());
        return "redirect:/groups/" + id;
    }

    /** Sends a member to Discord to approve connecting their account. */
    @GetMapping("/groups/{id}/discord/connect")
    public String connect(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            return errorRedirect(redirectAttributes, "Only students can join a group chat", "/home");
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        try {
            return "redirect:" + discordService.startConnect(group, student);
        } catch (BusinessRuleException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/" + id);
        }
    }

    /** Where Discord sends the student back after they approve (code) or cancel (error). */
    @GetMapping("/discord/callback")
    public String callback(@RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null) {
            return errorRedirect(redirectAttributes, "Only students can join a group chat", "/home");
        }
        if (error != null) {
            String groupId = discordService.cancelConnect(state, student.getId());
            return errorRedirect(redirectAttributes, "Discord was not connected",
                    groupId == null ? "/groups/mine" : "/groups/" + groupId);
        }
        String groupId;
        try {
            groupId = discordService.finishConnect(state, code, student.getId());
        } catch (BusinessRuleException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/mine");
        }
        Toasts.success(redirectAttributes, "Discord connected. Your group chats are now in the server.");
        return "redirect:/groups/" + groupId;
    }

    /** Adds an error toast and redirects to the given path. */
    private String errorRedirect(RedirectAttributes redirectAttributes, String message, String path) {
        Toasts.error(redirectAttributes, message);
        return "redirect:" + path;
    }
}
