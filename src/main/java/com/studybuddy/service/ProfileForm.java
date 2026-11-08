package com.studybuddy.service;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.model.Course;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyPreferences;

/** Raw values from the edit-profile form, all as text; ProfileEditService validates and applies them. */
public class ProfileForm {

    private String name;
    private String school;
    private String programme;
    private String year;
    private String newContactNumber;
    private List<String> currentCourseCodes = new ArrayList<>();
    private List<String> completedCourseCodes = new ArrayList<>();
    private String targetCourseCode;
    private String mode;
    private String format;
    private List<String> goals = new ArrayList<>();
    private String intensity;
    private String lookingFor;

    /** Creates an empty form; the controller fills it from the request parameters. */
    public ProfileForm() {
    }

    /**
     * Creates a form filled with the profile's current values, for the edit page. The contact
     * number is left blank: StudentProfile has no getter for it, and blank means "keep it".
     *
     * @param profile the profile to copy from
     * @return the prefilled form
     */
    public static ProfileForm from(StudentProfile profile) {
        ProfileForm form = new ProfileForm();
        form.setSchool(profile.getSchool());
        form.setProgramme(profile.getProgramme());
        form.setYear(String.valueOf(profile.getYearOfStudy()));
        form.setCurrentCourseCodes(codesOf(profile.getCoursesTaken()));
        form.setCompletedCourseCodes(codesOf(profile.getCompletedCourses()));
        form.setLookingFor(profile.getLookingFor());

        StudyPreferences preferences = profile.getPreferences();
        if (preferences != null) {
            if (preferences.getTargetCourse() != null) {
                form.setTargetCourseCode(preferences.getTargetCourse().getCode());
            }
            if (preferences.getMode() != null) {
                form.setMode(preferences.getMode().name());
            }
            if (preferences.getFormat() != null) {
                form.setFormat(preferences.getFormat().name());
            }
            if (preferences.getIntensity() != null) {
                form.setIntensity(preferences.getIntensity().name());
            }
            List<String> goalNames = new ArrayList<>();
            for (StudyGoal goal : preferences.getGoals()) {
                goalNames.add(goal.name());
            }
            form.setGoals(goalNames);
        }
        return form;
    }

    /** Returns the codes of the given courses, in the same order. */
    private static List<String> codesOf(List<Course> courses) {
        List<String> codes = new ArrayList<>();
        for (Course course : courses) {
            codes.add(course.getCode());
        }
        return codes;
    }

    /** Returns the name as typed. */
    public String getName() {
        return name;
    }

    /** Sets the name as typed. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the school as typed. */
    public String getSchool() {
        return school;
    }

    /** Sets the school as typed. */
    public void setSchool(String school) {
        this.school = school;
    }

    /** Returns the programme as typed. */
    public String getProgramme() {
        return programme;
    }

    /** Sets the programme as typed. */
    public void setProgramme(String programme) {
        this.programme = programme;
    }

    /** Returns the year of study as typed (not yet checked to be a number). */
    public String getYear() {
        return year;
    }

    /** Sets the year of study as typed. */
    public void setYear(String year) {
        this.year = year;
    }

    /** Returns the new contact number as typed; blank means keep the current one. */
    public String getNewContactNumber() {
        return newContactNumber;
    }

    /** Sets the new contact number as typed. */
    public void setNewContactNumber(String newContactNumber) {
        this.newContactNumber = newContactNumber;
    }

    /** Returns the ticked current course codes. */
    public List<String> getCurrentCourseCodes() {
        return currentCourseCodes;
    }

    /** Sets the ticked current course codes. */
    public void setCurrentCourseCodes(List<String> currentCourseCodes) {
        this.currentCourseCodes = currentCourseCodes;
    }

    /** Returns the ticked completed course codes. */
    public List<String> getCompletedCourseCodes() {
        return completedCourseCodes;
    }

    /** Sets the ticked completed course codes. */
    public void setCompletedCourseCodes(List<String> completedCourseCodes) {
        this.completedCourseCodes = completedCourseCodes;
    }

    /** Returns the chosen target course code. */
    public String getTargetCourseCode() {
        return targetCourseCode;
    }

    /** Sets the chosen target course code. */
    public void setTargetCourseCode(String targetCourseCode) {
        this.targetCourseCode = targetCourseCode;
    }

    /** Returns the chosen study mode, as a StudyMode name. */
    public String getMode() {
        return mode;
    }

    /** Sets the chosen study mode, as a StudyMode name. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** Returns the chosen group format, as a GroupFormat name. */
    public String getFormat() {
        return format;
    }

    /** Sets the chosen group format, as a GroupFormat name. */
    public void setFormat(String format) {
        this.format = format;
    }

    /** Returns the ticked study goals, as StudyGoal names. */
    public List<String> getGoals() {
        return goals;
    }

    /** Sets the ticked study goals, as StudyGoal names. */
    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    /** Returns the chosen intensity, as an Intensity name. */
    public String getIntensity() {
        return intensity;
    }

    /** Sets the chosen intensity, as an Intensity name. */
    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    /** Returns the "looking for" text as typed. */
    public String getLookingFor() {
        return lookingFor;
    }

    /** Sets the "looking for" text as typed. */
    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }
}
