package com.studybuddy.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.studybuddy.service.UserService;

/**
 * Registers AuthInterceptor for every page except the user picker, sign-in, sign-out, static
 * files and the error page. Bare /admin is also left out: it only redirects to /admin/users,
 * which the interceptor then checks.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;

    /** Creates the configuration with the service the interceptor needs (constructor injection). */
    public WebConfig(UserService userService) {
        this.userService = userService;
    }

    /** Adds the sign-in and admin-area check in front of the controllers. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(userService))
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/sign-in", "/sign-out", "/admin", "/css/**", "/js/**", "/error");
    }
}
