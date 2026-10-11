package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studybuddy.model.User;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** The signed-in user's home page (placeholder until the real one is built). */
@Controller
public class HomeController {

    private final UserService userService;

    /** Creates the controller with the service it delegates to (constructor injection). */
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    /** Shows the home page, or sends the visitor to the picker if nobody is signed in. */
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        String userId = CurrentUser.getId(session);
        if (userId == null) {
            return "redirect:/";
        }
        User user = userService.findUser(userId);
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        model.addAttribute("student", userService.findStudent(userId));   // null for an admin
        return "shared/home";                // -> templates/shared/home.html
    }
}
