package com.studybuddy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.Course;
import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.Intensity;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.StudyPreferences;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * A student editing their OWN profile: validating and applying the details form, and saving
 * the availability grid. Who may see what stays in ProfileService; editing is a separate job.
 */
@Service
public class ProfileEditService {

    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 5;
    private static final int MAX_LOOKING_FOR_LENGTH = 200;
    private static final String CONTACT_PATTERN = "[89][0-9]{7}";
    private static final int DAYS = 7;
    private static final int HOURS = 15;
    private static final String CELL_SEPARATOR = "-";

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    /** Creates the service with the repositories it uses (constructor injection). */
    public ProfileEditService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /** Returns field name to message for every invalid field; empty when the form is valid. */
    public Map<String, String> validate(ProfileForm form) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (trimOrEmpty(form.getSchool()).isEmpty()) {
            fieldErrors.put("school", "School is required");
        }
        if (trimOrEmpty(form.getProgramme()).isEmpty()) {
            fieldErrors.put("programme", "Programme is required");
        }

        Integer year = parseWholeNumber(form.getYear());
        if (year == null) {
            fieldErrors.put("year", "Year must be a whole number from " + MIN_YEAR + " to " + MAX_YEAR);
        } else if (year < MIN_YEAR || year > MAX_YEAR) {
            fieldErrors.put("year", "Year must be from " + MIN_YEAR + " to " + MAX_YEAR);
        }

        String contact = trimOrEmpty(form.getNewContactNumber());
        if (!contact.isEmpty() && !contact.matches(CONTACT_PATTERN)) {
            fieldErrors.put("newContactNumber", "Enter 8 digits starting with 8 or 9");
        }

        List<String> current = listOrEmpty(form.getCurrentCourseCodes());
        List<String> completed = listOrEmpty(form.getCompletedCourseCodes());
        if (current.isEmpty()) {
            fieldErrors.put("currentCourseCodes", "Pick at least one current course");
        } else if (!allKnownCourses(current)) {
            fieldErrors.put("currentCourseCodes", "Pick courses from the list");
        }
        if (!allKnownCourses(completed)) {
            fieldErrors.put("completedCourseCodes", "Pick courses from the list");
        } else {
            for (String code : completed) {
                if (current.contains(code)) {
                    fieldErrors.put("completedCourseCodes", code + " can't be both current and completed");
                    break;
                }
            }
        }

        String target = trimOrEmpty(form.getTargetCourseCode());
        if (target.isEmpty() || !current.contains(target)) {
            fieldErrors.put("targetCourseCode", "Pick a target course you're currently taking");
        }

        if (toMode(form.getMode()) == null) {
            fieldErrors.put("mode", "Choose a study mode");
        }
        if (toFormat(form.getFormat()) == null) {
            fieldErrors.put("format", "Choose a group size");
        }
        if (toIntensity(form.getIntensity()) == null) {
            fieldErrors.put("intensity", "Choose an intensity");
        }

        List<String> goals = listOrEmpty(form.getGoals());
        if (goals.isEmpty()) {
            fieldErrors.put("goals", "Pick at least one study goal");
        } else {
            for (String goal : goals) {
                if (toGoal(goal) == null) {
                    fieldErrors.put("goals", "Pick study goals from the list");
                    break;
                }
            }
        }

        if (trimOrEmpty(form.getLookingFor()).length() > MAX_LOOKING_FOR_LENGTH) {
            fieldErrors.put("lookingFor", "Keep this to " + MAX_LOOKING_FOR_LENGTH + " characters or fewer");
        }

        return fieldErrors;
    }

    /**
     * Copies a validated form onto the student's profile. Courses are looked up by code; the
     * contact number changes only when a new one was typed.
     *
     * @param student the signed-in student
     * @param form a form that has already passed validate
     */
    public void update(Student student, ProfileForm form) {
        StudentProfile profile = student.getProfile();
        profile.setSchool(trimOrEmpty(form.getSchool()));
        profile.setProgramme(trimOrEmpty(form.getProgramme()));
        profile.setYearOfStudy(parseWholeNumber(form.getYear()));
        String contact = trimOrEmpty(form.getNewContactNumber());
        if (!contact.isEmpty()) {
            profile.setContactNumber(contact);
        }
        profile.setCoursesTaken(toCourses(form.getCurrentCourseCodes()));
        profile.setCompletedCourses(toCourses(form.getCompletedCourseCodes()));
        profile.setLookingFor(trimOrEmpty(form.getLookingFor()));

        StudyPreferences preferences = profile.getPreferences();
        if (preferences == null) {
            preferences = new StudyPreferences();
            profile.setPreferences(preferences);
        }
        preferences.setTargetCourse(courseRepository.findByCode(trimOrEmpty(form.getTargetCourseCode())));
        preferences.setMode(toMode(form.getMode()));
        preferences.setFormat(toFormat(form.getFormat()));
        preferences.setIntensity(toIntensity(form.getIntensity()));
        preferences.setGoals(toGoals(form.getGoals()));
        studentRepository.save(student);
    }

    /**
     * Replaces the student's availability with exactly the ticked cells. Each cell is
     * "day-hourIndex", e.g. "2-11" for Wednesday 19:00; malformed values are ignored.
     *
     * @param student the signed-in student
     * @param cells the ticked cell values, or null when nothing was ticked
     */
    public void updateAvailability(Student student, List<String> cells) {
        AvailabilityGrid grid = new AvailabilityGrid();
        for (String cell : listOrEmpty(cells)) {
            String[] parts = trimOrEmpty(cell).split(CELL_SEPARATOR);
            if (parts.length != 2) {
                continue;
            }
            Integer day = parseWholeNumber(parts[0]);
            Integer hour = parseWholeNumber(parts[1]);
            if (day == null || hour == null || day < 0 || day >= DAYS || hour < 0 || hour >= HOURS) {
                continue;
            }
            grid.set(day, hour, true);
        }
        student.getProfile().setAvailability(grid);
        studentRepository.save(student);
    }

    /** Returns true when the student has not marked a single free hour yet. */
    public boolean needsAvailability(Student student) {
        StudentProfile profile = student.getProfile();
        if (profile == null || profile.getAvailability() == null) {
            return true;
        }
        return AvailabilityFormatter.format(profile.getAvailability()).isEmpty();
    }

    /** Returns true if every code names a course in the catalog. */
    private boolean allKnownCourses(List<String> codes) {
        for (String code : codes) {
            if (courseRepository.findByCode(code) == null) {
                return false;
            }
        }
        return true;
    }

    /** Looks up each code in the catalog, skipping unknown ones. */
    private List<Course> toCourses(List<String> codes) {
        List<Course> courses = new ArrayList<>();
        for (String code : listOrEmpty(codes)) {
            Course course = courseRepository.findByCode(code);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }

    /** Converts goal names to a set of StudyGoal in the enum's order, skipping unknown names. */
    private Set<StudyGoal> toGoals(List<String> names) {
        Set<StudyGoal> goals = new LinkedHashSet<>();
        List<String> chosen = listOrEmpty(names);
        for (StudyGoal goal : StudyGoal.values()) {
            if (chosen.contains(goal.name())) {
                goals.add(goal);
            }
        }
        return goals;
    }

    /** Returns the StudyGoal with this name, or null if there is none. */
    private StudyGoal toGoal(String text) {
        for (StudyGoal goal : StudyGoal.values()) {
            if (goal.name().equals(text)) {
                return goal;
            }
        }
        return null;
    }

    /** Returns the StudyMode with this name, or null if there is none. */
    private StudyMode toMode(String text) {
        for (StudyMode mode : StudyMode.values()) {
            if (mode.name().equals(text)) {
                return mode;
            }
        }
        return null;
    }

    /** Returns the GroupFormat with this name, or null if there is none. */
    private GroupFormat toFormat(String text) {
        for (GroupFormat format : GroupFormat.values()) {
            if (format.name().equals(text)) {
                return format;
            }
        }
        return null;
    }

    /** Returns the Intensity with this name, or null if there is none. */
    private Intensity toIntensity(String text) {
        for (Intensity intensity : Intensity.values()) {
            if (intensity.name().equals(text)) {
                return intensity;
            }
        }
        return null;
    }

    /** Returns the list, or an empty list when it is null. */
    private List<String> listOrEmpty(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    /** Returns the text trimmed, or an empty string when it is null. */
    private String trimOrEmpty(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    /** Returns the text as a whole number, or null if it is not one. */
    private Integer parseWholeNumber(String text) {
        try {
            return Integer.parseInt(trimOrEmpty(text));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
