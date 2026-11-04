package com.studybuddy.controller;

import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adds a toast message for the next page (Post/Redirect/Get). The toast fragment reads the two
 * flash attributes set here. Static methods only; it is a helper, not a Spring bean.
 */
public final class Toasts {

    private static final String MESSAGE = "toastMessage";
    private static final String TYPE = "toastType";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    private Toasts() {
    }

    /** Shows a success toast with this message on the next page. */
    public static void success(RedirectAttributes redirectAttributes, String message) {
        add(redirectAttributes, message, SUCCESS);
    }

    /** Shows an error toast with this message on the next page. */
    public static void error(RedirectAttributes redirectAttributes, String message) {
        add(redirectAttributes, message, ERROR);
    }

    /**
     * Shows an error toast on the page at redirectPath, for code that sends its own redirect
     * outside a controller method (AuthInterceptor). Call it before sending the redirect.
     */
    public static void errorBeforeRedirect(HttpServletRequest request, HttpServletResponse response,
            String message, String redirectPath) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put(MESSAGE, message);
        flashMap.put(TYPE, ERROR);
        RequestContextUtils.saveOutputFlashMap(redirectPath, request, response);
    }

    /** Stores the message and its type as flash attributes. */
    private static void add(RedirectAttributes redirectAttributes, String message, String type) {
        redirectAttributes.addFlashAttribute(MESSAGE, message);
        redirectAttributes.addFlashAttribute(TYPE, type);
    }
}
