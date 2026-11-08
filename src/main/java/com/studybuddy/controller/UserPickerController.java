package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.User;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** The user picker: choose who you are for this session, and switch user. */
@Controller
public class UserPickerController {

    private final UserService userService;

    /** Creates the controller with the service it delegates to (constructor injection). */
    public UserPickerController(UserService userService) {
        this.userService = userService;
    }

    /** Shows the list of students and administrators to sign in as. */
    @GetMapping("/")
    public String showPicker(Model model) {
        model.addAttribute("admins", userService.findAllAdmins()); 
        model.addAttribute("students", userService.findAllStudents());
        return "shared/user-picker";         // -> templates/shared/user-picker.html
    }

    /** Signs in as the chosen user, unless the id is unknown or the account is suspended. */
    @PostMapping("/sign-in")
    public String signIn(@RequestParam String userId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = userService.findUser(userId);
        if (user == null) {
            Toasts.error(redirectAttributes, "Choose a user to continue");
            return "redirect:/";
        }
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            Toasts.error(redirectAttributes, "This account is suspended");
            return "redirect:/";
        }
        userService.recordSignIn(user);
        CurrentUser.signIn(session, user);
        Toasts.success(redirectAttributes, "Signed in as " + user.getName());
        return "redirect:/home";
    }

    /** Signs out and goes back to the picker. */
    @PostMapping("/sign-out")
    public String signOut(HttpSession session) {
        CurrentUser.signOut(session);
        return "redirect:/";
    }
}
