package com.studybuddy.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.studybuddy.service.NavigationService;
import com.studybuddy.service.UserService;

/**
 * Registers AuthInterceptor for every page except the user picker, sign-in, sign-out, static
 * files and the error page. Bare /admin is also left out: it only redirects to /admin/users,
 * which the interceptor then checks.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final NavigationService navigationService;

    /** Creates the configuration with the services the interceptor needs (constructor injection). */
    public WebConfig(UserService userService, NavigationService navigationService) {
        this.userService = userService;
        this.navigationService = navigationService;
    }

    /** Adds the sign-in and admin-area check in front of the controllers. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(userService, navigationService))
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/sign-in", "/sign-out", "/admin", "/css/**", "/js/**", "/error");
    }
}
