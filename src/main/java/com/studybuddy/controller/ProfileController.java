package com.studybuddy.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studybuddy.importer.TimetableImport;
import com.studybuddy.importer.TimetableImportException;
import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.Intensity;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyMode;
import com.studybuddy.service.CourseService;
import com.studybuddy.service.ProfileEditService;
import com.studybuddy.service.ProfileForm;
import com.studybuddy.service.ProfileService;
import com.studybuddy.service.TimetableImportService;
import com.studybuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

/** My profile: viewing and editing the signed-in student's own details and weekly availability, and timetable import. */
@Controller
public class ProfileController {

    private static final String TIMETABLE_FIELD = "timetable";
    private static final String NOT_A_STUDENT = "My profile is for students";
    private static final String TOO_LARGE_MESSAGE =
            "That file is too large to upload. A timetable export is usually only a few KB.";

    private final ProfileService profileService;
    private final ProfileEditService editService;
    private final TimetableImportService importService;
    private final UserService userService;
    private final CourseService courseService;

    /** Creates the controller with the services it delegates to (constructor injection). */
    public ProfileController(ProfileService profileService, ProfileEditService editService,
            TimetableImportService importService, UserService userService, CourseService courseService) {
        this.profileService = profileService;
        this.editService = editService;
        this.importService = importService;
        this.userService = userService;
        this.courseService = courseService;
    }

    /** Shows the signed-in student's own profile, contact number included. */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        model.addAttribute("student", student);
        model.addAttribute("profile", profileService.ownView(student));
        model.addAttribute("grid", new AvailabilityGridView(student.getProfile().getAvailability(), null));
        return "student/profile";
    }

    /** Shows the edit-details form filled in with the current values (contact left blank). */
    @GetMapping("/profile/edit")
    public String showEditForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        ProfileForm form = ProfileForm.from(student.getProfile());
        form.setName(student.getName());
        model.addAttribute("form", form);
        model.addAttribute("fieldErrors", new HashMap<String, String>());
        addFormOptions(model);
        return "student/profile-edit";
    }

    /** Saves the details form; re-shows it with field errors, or returns to the profile with a toast. */
    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam(required = false) String name,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String programme,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String newContactNumber,
            @RequestParam(required = false) List<String> currentCourseCodes,
            @RequestParam(required = false) List<String> completedCourseCodes,
            @RequestParam(required = false) String targetCourseCode,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) List<String> goals,
            @RequestParam(required = false) String intensity,
            @RequestParam(required = false) String lookingFor,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        ProfileForm form = new ProfileForm();
        form.setName(name);
        form.setSchool(school);
        form.setProgramme(programme);
        form.setYear(year);
        form.setNewContactNumber(newContactNumber);
        if (currentCourseCodes != null) {
            form.setCurrentCourseCodes(currentCourseCodes);
        }
        if (completedCourseCodes != null) {
            form.setCompletedCourseCodes(completedCourseCodes);
        }
        form.setTargetCourseCode(targetCourseCode);
        form.setMode(mode);
        form.setFormat(format);
        if (goals != null) {
            form.setGoals(goals);
        }
        form.setIntensity(intensity);
        form.setLookingFor(lookingFor);

        Map<String, String> fieldErrors = editService.validate(form);
        if (!fieldErrors.isEmpty()) {
            model.addAttribute("form", form);
            model.addAttribute("fieldErrors", fieldErrors);
            addFormOptions(model);
            return "student/profile-edit";
        }
        editService.update(student, form);
        CurrentUser.updateName(session, student.getName());
        Toasts.success(redirectAttributes, "Profile updated");
        return "redirect:/profile";
    }

    /** Shows the availability editor with the current grid ticked. */
    @GetMapping("/profile/availability")
    public String showAvailability(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        model.addAttribute("grid", new AvailabilityGridView(student.getProfile().getAvailability(), null));
        return "student/availability-edit";
    }

    /** Saves exactly the ticked cells as the student's availability. */
    @PostMapping("/profile/availability")
    public String saveAvailability(@RequestParam(required = false) List<String> cells, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        editService.updateAvailability(student, cells);
        Toasts.success(redirectAttributes, "Availability saved");
        return "redirect:/profile";
    }

    /**
     * Reads an uploaded timetable and re-shows the editor with a draft: class hours marked and
     * disabled, every other hour ticked. Nothing is saved until the student clicks Save.
     * The file is read from the request here, not as a parameter, so that a file over the
     * size limit (spring.servlet.multipart.max-file-size) shows the error banner instead of
     * an error page; application.properties turns on lazy multipart parsing for this.
     */
    @PostMapping("/profile/availability/import")
    public String importTimetable(MultipartHttpServletRequest request, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        Student student = CurrentUser.studentOrNull(session, userService);
        if (student == null || student.getProfile() == null) {
            Toasts.error(redirectAttributes, NOT_A_STUDENT);
            return "redirect:/home";
        }
        AvailabilityGridView currentGrid = new AvailabilityGridView(student.getProfile().getAvailability(), null);

        MultipartFile file;
        try {
            file = request.getFile(TIMETABLE_FIELD);
        } catch (MultipartException e) {
            return showImportError(model, currentGrid, TOO_LARGE_MESSAGE);
        }
        if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            return showImportError(model, currentGrid, "Choose an .ics file to upload");
        }
        if (file.isEmpty()) {
            return showImportError(model, currentGrid, "That file is empty. Export your timetable again and upload it.");
        }

        try (InputStream input = file.getInputStream()) {
            TimetableImport imported = importService.importTimetable(file.getOriginalFilename(), input);
            model.addAttribute("grid", new AvailabilityGridView(importService.draftFrom(imported), imported.getBusy()));
            model.addAttribute("imported", imported);
            return "student/availability-edit";
        } catch (TimetableImportException e) {
            return showImportError(model, currentGrid, e.getMessage());
        } catch (IOException e) {
            return showImportError(model, currentGrid, "The uploaded file could not be read");
        }
    }

    /** Re-shows the availability editor unchanged, with an error banner. */
    private String showImportError(Model model, AvailabilityGridView grid, String message) {
        model.addAttribute("grid", grid);
        model.addAttribute("importError", message);
        return "student/availability-edit";
    }

    /** Adds the lists the edit form's checkboxes and selects are built from. */
    private void addFormOptions(Model model) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("modeOptions", StudyMode.values());
        model.addAttribute("formatOptions", GroupFormat.values());
        model.addAttribute("intensityOptions", Intensity.values());
        model.addAttribute("goalOptions", StudyGoal.values());
    }
}
