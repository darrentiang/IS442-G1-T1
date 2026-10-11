package com.studybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studybuddy.service.CourseService;

import jakarta.servlet.http.HttpSession;

/** Pages about courses. */
@Controller
public class CourseController {

    private final CourseService courseService;

    /** Creates the controller with the service it delegates to (constructor injection). */
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** Shows the table of all courses, or sends the visitor to the picker if nobody is signed in. */
    @GetMapping("/courses")
    public String showCourses(HttpSession session, Model model) {
        if (CurrentUser.getId(session) == null) {
            return "redirect:/";
        }
        model.addAttribute("courses", courseService.findAll());
        return "shared/courses";             // -> templates/shared/courses.html
    }
}
