package com.studybuddy.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.model.Role;
import com.studybuddy.model.User;
import com.studybuddy.service.AdminService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** Pages for the system administrator: the account list and one account's detail. */
@Controller
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    /** Sends /admin to the account list, the admin area's landing page. */
    @GetMapping("/admin")
    public String showAdminHome() {
        return "redirect:/admin/users";
    }

    /** Shows every account, or sends the visitor away if no administrator is signed in. */
    @GetMapping("/admin/users")
    public String showUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    /** Shows one account's detail, or sends the visitor away if no administrator is signed in. */
    @GetMapping("/admin/users/{id}")
    public String showUser(@PathVariable String id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        User user = userService.findUser(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    /** Shows the blank form for a new account, or sends the visitor away if no administrator is signed in. */
    @GetMapping("/admin/users/new")
    public String showNewForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        return "admin/user-create";
    }

    /** Creates an account from the form; re-shows the form with field errors, or returns to the list with a toast. */
    @PostMapping("/admin/users")
    public String createAccount(@RequestParam String role, @RequestParam String name, @RequestParam String email,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        Map<String, String> fieldErrors = adminService.createAccount(role, name, email);
        if (!fieldErrors.isEmpty()) {
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("role", role);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "admin/user-create";
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Account created");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/users";
    }

    /** Shows the edit form for one account filled in with its current values, or the list if there is no such account. */
    @GetMapping("/admin/users/{id}/edit")
    public String showEditForm(@PathVariable String id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        User user = userService.findUser(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("name", user.getName());
        model.addAttribute("email", user.getEmail());
        return "admin/user-edit";
    }

    /** Updates an account from the form; re-shows the form with field errors, or returns to the account with a toast. */
    @PostMapping("/admin/users/{id}")
    public String updateAccount(@PathVariable String id, @RequestParam String name, @RequestParam String email,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        if (userService.findUser(id) == null) {
            return "redirect:/admin/users";
        }
        Map<String, String> fieldErrors = adminService.updateAccount(id, name, email);
        if (!fieldErrors.isEmpty()) {
            model.addAttribute("user", userService.findUser(id));
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "admin/user-edit";
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Account updated");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/users/" + id;
    }

    /** Suspends or reactivates an account, or reports why the change is not allowed; returns to the account with a toast. */
    @PostMapping("/admin/users/{id}/status")
    public String changeStatus(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        String error = adminService.changeStatus(id, CurrentUser.getId(session));
        if (error != null) {
            redirectAttributes.addFlashAttribute("toastMessage", error);
            redirectAttributes.addFlashAttribute("toastType", "error");
        } else {
            redirectAttributes.addFlashAttribute("toastMessage", "Account status updated");
            redirectAttributes.addFlashAttribute("toastType", "success");
        }
        return "redirect:/admin/users/" + id;
    }

    /** Deletes an account, or reports why it cannot be deleted; returns to the account list with a toast. */
    @PostMapping("/admin/users/{id}/delete")
    public String deleteAccount(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        String error = adminService.deleteAccount(id, CurrentUser.getId(session));
        if (error != null) {
            redirectAttributes.addFlashAttribute("toastMessage", error);
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/admin/users/" + id;
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Account deleted");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/users";
    }

    /** Returns whether an administrator is signed in for this session. */
    private boolean isAdmin(HttpSession session) {
        return CurrentUser.getRole(session) == Role.ADMIN;
    }
}
