package com.studybuddy.controller;

import java.io.IOException;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Role;
import com.studybuddy.model.User;
import com.studybuddy.service.NavigationService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Checks every page request before its controller runs: the visitor must be signed in as a
 * user that still exists and is not suspended, and only an administrator may open the admin
 * area. A check every page needs is a cross-cutting concern, so it lives here once instead of
 * in every controller. WebConfig decides which paths it guards. After the controller runs, it
 * also adds the navigation's pending counts for a signed-in student, which every page shows.
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final String ADMIN_AREA = "/admin/";
    private static final String PICKER = "/";
    private static final String SUSPENDED_MESSAGE = "This account is suspended";
    private static final String REDIRECT = "redirect:";
    private static final String NAV_COUNTS = "navCounts";

    private final UserService userService;
    private final NavigationService navigationService;

    /** Creates the interceptor with the services it looks users and navigation counts up in. */
    public AuthInterceptor(UserService userService, NavigationService navigationService) {
        this.userService = userService;
        this.navigationService = navigationService;
    }

    /**
     * Lets the request through, or sends the visitor to the user picker and stops it. A user
     * suspended while signed in is signed out and told why; every other refusal has no toast.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession();
        String userId = CurrentUser.getId(session);
        User user = (userId == null) ? null : userService.findUser(userId);
        if (user == null) {
            return redirectToPicker(request, response);
        }
        if (user.getStatus() != AccountStatus.ACTIVE) {
            CurrentUser.signOut(session);
            Toasts.errorBeforeRedirect(request, response, SUSPENDED_MESSAGE, PICKER);
            return redirectToPicker(request, response);
        }
        if (isAdminArea(request) && CurrentUser.getRole(session) != Role.ADMIN) {
            return redirectToPicker(request, response);
        }
        return true;
    }

    /** Adds the navigation's pending counts to a page shown to a student; redirects and admin pages get none. */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        if (modelAndView == null || modelAndView.getViewName() == null
                || modelAndView.getViewName().startsWith(REDIRECT)) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null || CurrentUser.getRole(session) != Role.STUDENT) {
            return;
        }
        modelAndView.addObject(NAV_COUNTS, navigationService.countsFor(CurrentUser.getId(session)));
    }

    /** Returns true for paths under /admin/. */
    private boolean isAdminArea(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith(ADMIN_AREA);
    }

    /** Redirects to the user picker and returns false, so the controller does not run. */
    private boolean redirectToPicker(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + PICKER);
        return false;
    }
}
