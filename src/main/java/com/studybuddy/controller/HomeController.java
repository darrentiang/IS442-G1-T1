package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studybuddy.model.Student;
import com.studybuddy.model.User;
import com.studybuddy.service.ProfileEditService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** The signed-in user's home page (placeholder until the real one is built). */
@Controller
public class HomeController {

    private final UserService userService;
    private final ProfileEditService editService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public HomeController(UserService userService, ProfileEditService editService) {
        this.userService = userService;
        this.editService = editService;
    }

    /** Shows the home page for the signed-in user. */
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        String userId = CurrentUser.getId(session);
        User user = userService.findUser(userId);
        Student student = userService.findStudent(userId);                // null for an admin
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("needsAvailability", student != null && editService.needsAvailability(student));
        return "shared/home";                // -> templates/shared/home.html
    }
}
