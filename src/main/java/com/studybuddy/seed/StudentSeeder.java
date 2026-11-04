package com.studybuddy.seed;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Course;
import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.Intensity;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.StudyPreferences;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Loads students.csv, each with a full profile. Columns: id,name,email,status,school,programme,
 * year,contact,current_courses,completed_courses,target_course,mode,format,goals,intensity,
 * availability,looking_for. Courses must already be loaded.
 */
public class StudentSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "students.csv";
    private static final int COLUMNS = 17;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    /** Creates the seeder with the repository it fills and the one it looks courses up in. */
    public StudentSeeder(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /** Returns "students.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one student with profile and preferences per row. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);

            Student student = new Student(cells[0], cells[1], cells[2]);
            student.setStatus(file.parseEnum(AccountStatus.class, cells[3], "status", line));

            StudentProfile profile = new StudentProfile();
            profile.setSchool(cells[4]);
            profile.setProgramme(cells[5]);
            profile.setYearOfStudy(file.parseWholeNumber(cells[6], "year", line));
            profile.setContactNumber(cells[7]);
            profile.setCoursesTaken(parseCourseList(file, cells[8], line));
            profile.setCompletedCourses(parseCourseList(file, cells[9], line));

            StudyPreferences preferences = new StudyPreferences();
            preferences.setTargetCourse(findCourse(file, cells[10], line));
            preferences.setMode(file.parseEnum(StudyMode.class, cells[11], "mode", line));
            preferences.setFormat(file.parseEnum(GroupFormat.class, cells[12], "format", line));
            preferences.setGoals(file.parseGoals(cells[13], line));
            preferences.setIntensity(file.parseEnum(Intensity.class, cells[14], "intensity", line));
            profile.setPreferences(preferences);

            profile.setAvailability(file.parseAvailability(cells[15], line));
            profile.setLookingFor(cells[16]);

            student.setProfile(profile);
            studentRepository.save(student);
            count++;
        }
        return count;
    }

    /** Turns "IS442|IS215" into a list of courses; an empty cell gives an empty list. */
    private List<Course> parseCourseList(CsvFile file, String cell, int line) throws SeedDataException {
        List<Course> courses = new ArrayList<>();
        for (String code : file.splitList(cell)) {
            courses.add(findCourse(file, code, line));
        }
        return courses;
    }

    /** Looks up a course by code; an unknown code is a seed error. */
    private Course findCourse(CsvFile file, String code, int line) throws SeedDataException {
        return file.require(courseRepository.findByCode(code), "course code", code, line);
    }
}
