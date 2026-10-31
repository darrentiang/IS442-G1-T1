package com.studybuddy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.Visibility;
import com.studybuddy.service.AvailabilityFormatter;
import com.studybuddy.service.CourseService;
import com.studybuddy.service.GroupForm;
import com.studybuddy.service.StudyGroupService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** Pages for group leaders: create, edit, close and reopen a group, and the student's own groups. */
@Controller
public class GroupController {

    private final StudyGroupService groupService;
    private final CourseService courseService;
    private final UserService userService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public GroupController(StudyGroupService groupService, CourseService courseService, UserService userService) {
        this.groupService = groupService;
        this.courseService = courseService;
        this.userService = userService;
    }

    /** Shows the empty create-group form. */
    @GetMapping("/groups/new")
    public String showCreateForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students can create groups", "/home");
        }
        model.addAttribute("form", new GroupForm());
        model.addAttribute("fieldErrors", new HashMap<String, String>());
        addFormOptions(model);
        return "group/form";
    }

    /** Creates a group from the form; re-shows the form with field errors, or opens the new group with a toast. */
    @PostMapping("/groups")
    public String createGroup(@RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) List<String> goals,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String availabilityText,
            @RequestParam(required = false) String maxSize,
            @RequestParam(required = false) String visibility,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        Student student = userService.findStudent(userId);
        if (student == null) {
            return errorRedirect(redirectAttributes, "Only students can create groups", "/home");
        }
        GroupForm form = buildForm(name, description, courseCode, goals, mode, availabilityText, maxSize, visibility);
        Map<String, String> fieldErrors = groupService.validate(form, null);
        if (!fieldErrors.isEmpty()) {
            model.addAttribute("form", form);
            model.addAttribute("fieldErrors", fieldErrors);
            addFormOptions(model);
            return "group/form";
        }
        StudyGroup group = groupService.createGroup(student, form);
        return successRedirect(redirectAttributes, "Group created", "/groups/" + group.getId());
    }

    /** Shows the edit form filled in with the group's current values; leader only. */
    @GetMapping("/groups/{id}/edit")
    public String showEditForm(@PathVariable String id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students can manage groups", "/home");
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        if (!groupService.isLeader(group, userId)) {
            return errorRedirect(redirectAttributes, "Only the group leader can edit this group", "/groups/" + id);
        }
        model.addAttribute("form", formFrom(group));
        model.addAttribute("fieldErrors", new HashMap<String, String>());
        model.addAttribute("group", group);
        addFormOptions(model);
        return "group/form";
    }

    /** Updates a group from the form; re-shows the form with field errors, or returns to the group with a toast. */
    @PostMapping("/groups/{id}/edit")
    public String updateGroup(@PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<String> goals,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String availabilityText,
            @RequestParam(required = false) String maxSize,
            @RequestParam(required = false) String visibility,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students can manage groups", "/home");
        }
        StudyGroup group = groupService.findById(id);
        if (group == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        if (!groupService.isLeader(group, userId)) {
            return errorRedirect(redirectAttributes, "Only the group leader can edit this group", "/groups/" + id);
        }
        // The course never changes after creation, so the form keeps the group's own course.
        GroupForm form = buildForm(name, description, group.getCourse().getCode(), goals, mode,
                availabilityText, maxSize, visibility);
        Map<String, String> fieldErrors = groupService.validate(form, group);
        if (!fieldErrors.isEmpty()) {
            model.addAttribute("form", form);
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("group", group);
            addFormOptions(model);
            return "group/form";
        }
        groupService.updateGroup(group, form);
        return successRedirect(redirectAttributes, "Group updated", "/groups/" + id);
    }

    /** Closes the group and declines its pending requests; returns to the group with a toast. */
    @PostMapping("/groups/{id}/close")
    public String closeGroup(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students can manage groups", "/home");
        }
        if (groupService.findById(id) == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        try {
            groupService.closeGroup(id, userId);
        } catch (IllegalStateException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/" + id);
        }
        return successRedirect(redirectAttributes, "Group closed. Pending requests were declined.", "/groups/" + id);
    }

    /** Reopens a closed group for joining; returns to the group with a toast. */
    @PostMapping("/groups/{id}/reopen")
    public String reopenGroup(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students can manage groups", "/home");
        }
        if (groupService.findById(id) == null) {
            return errorRedirect(redirectAttributes, "This group could not be found", "/groups");
        }
        try {
            groupService.reopenGroup(id, userId);
        } catch (IllegalStateException exception) {
            return errorRedirect(redirectAttributes, exception.getMessage(), "/groups/" + id);
        }
        return successRedirect(redirectAttributes, "Group reopened", "/groups/" + id);
    }

    /** Shows the groups the signed-in student leads and the groups they are a member of. */
    @GetMapping("/groups/mine")
    public String showMyGroups(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = CurrentUser.getId(session);
        if (userId == null || userService.findUser(userId) == null) {
            return "redirect:/";
        }
        if (userService.findStudent(userId) == null) {
            return errorRedirect(redirectAttributes, "Only students have groups", "/home");
        }
        model.addAttribute("ledGroups", groupService.findLedBy(userId));
        model.addAttribute("joinedGroups", groupService.findJoinedBy(userId));
        return "group/my-groups";
    }

    /** Puts the raw request values into a GroupForm; no goals ticked becomes an empty list. */
    private GroupForm buildForm(String name, String description, String courseCode, List<String> goals,
            String mode, String availabilityText, String maxSize, String visibility) {
        GroupForm form = new GroupForm();
        form.setName(name);
        form.setDescription(description);
        form.setCourseCode(courseCode);
        if (goals != null) {
            form.setGoals(goals);
        }
        form.setMode(mode);
        form.setAvailabilityText(availabilityText);
        form.setMaxSize(maxSize);
        form.setVisibility(visibility);
        return form;
    }

    /** Fills a GroupForm from an existing group, for the edit page. */
    private GroupForm formFrom(StudyGroup group) {
        List<String> goalNames = new ArrayList<>();
        for (StudyGoal goal : group.getGoals()) {
            goalNames.add(goal.name());
        }
        return buildForm(group.getName(), group.getDescription(), group.getCourse().getCode(), goalNames,
                group.getMode().name(), AvailabilityFormatter.format(group.getAvailability()),
                String.valueOf(group.getMaxSize()), group.getVisibility().name());
    }

    /** Adds the choices the form's course dropdown, goal checkboxes, mode and visibility options need. */
    private void addFormOptions(Model model) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("goalOptions", StudyGoal.values());
        model.addAttribute("modeOptions", StudyMode.values());
        model.addAttribute("visibilityOptions", Visibility.values());
    }

    /** Adds an error toast and redirects to the given path. */
    private String errorRedirect(RedirectAttributes redirectAttributes, String message, String path) {
        redirectAttributes.addFlashAttribute("toastMessage", message);
        redirectAttributes.addFlashAttribute("toastType", "error");
        return "redirect:" + path;
    }

    /** Adds a success toast and redirects to the given path. */
    private String successRedirect(RedirectAttributes redirectAttributes, String message, String path) {
        redirectAttributes.addFlashAttribute("toastMessage", message);
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:" + path;
    }
}
