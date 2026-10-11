package com.studybuddy.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.Course;
import com.studybuddy.model.GroupFormat;
import com.studybuddy.model.Intensity;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.StudyPreferences;
import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.SystemAdministratorRepository;

/**
 * Loads the demo data from the CSV files in src/main/resources/seed/ once, when the app
 * starts. Any bad row stops the app from starting, with a message naming the file and line.
 */
@Component
public class SeedDataLoader implements CommandLineRunner {

    private static final String COURSES_FILE = "courses.csv";
    private static final String ADMINS_FILE = "admins.csv";
    private static final String STUDENTS_FILE = "students.csv";

    private static final int COURSE_COLUMNS = 3;
    private static final int ADMIN_COLUMNS = 3;
    private static final int STUDENT_COLUMNS = 17;

    private final CourseRepository courseRepository;
    private final SystemAdministratorRepository adminRepository;
    private final StudentRepository studentRepository;

    /** Creates the loader with the repositories it fills (constructor injection). */
    public SeedDataLoader(CourseRepository courseRepository,
            SystemAdministratorRepository adminRepository,
            StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
    }

    /** Loads courses, then admins, then students (students refer to courses, so courses go first). */
    @Override
    public void run(String... args) throws SeedDataException {
        int courses = loadCourses();
        int admins = loadAdmins();
        int students = loadStudents();
        String adminWord = (admins == 1) ? " admin, " : " admins, ";
        System.out.println("Seeded " + courses + " courses, " + admins + adminWord + students + " students");
    }

    /** Columns: code,title,school. */
    private int loadCourses() throws SeedDataException {
        List<String> lines = readLines(COURSES_FILE);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            String where = COURSES_FILE + " line " + (i + 1);
            String[] cells = splitRow(lines.get(i), COURSE_COLUMNS, where);
            courseRepository.save(new Course(cells[0], cells[1], cells[2]));
            count++;
        }
        return count;
    }

    /** Columns: id,name,email. */
    private int loadAdmins() throws SeedDataException {
        List<String> lines = readLines(ADMINS_FILE);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            String where = ADMINS_FILE + " line " + (i + 1);
            String[] cells = splitRow(lines.get(i), ADMIN_COLUMNS, where);
            adminRepository.save(new SystemAdministrator(cells[0], cells[1], cells[2]));
            count++;
        }
        return count;
    }

    /**
     * Columns: id,name,email,status,school,programme,year,contact,current_courses,
     * completed_courses,target_course,mode,format,goals,intensity,availability,looking_for.
     */
    private int loadStudents() throws SeedDataException {
        List<String> lines = readLines(STUDENTS_FILE);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            String where = STUDENTS_FILE + " line " + (i + 1);
            String[] cells = splitRow(lines.get(i), STUDENT_COLUMNS, where);

            Student student = new Student(cells[0], cells[1], cells[2]);
            student.setStatus(parseEnum(AccountStatus.class, cells[3], "status", where));

            StudentProfile profile = new StudentProfile();
            profile.setSchool(cells[4]);
            profile.setProgramme(cells[5]);
            profile.setYearOfStudy(parseYear(cells[6], where));
            profile.setContactNumber(cells[7]);
            profile.setCoursesTaken(parseCourseList(cells[8], where));
            profile.setCompletedCourses(parseCourseList(cells[9], where));

            StudyPreferences preferences = new StudyPreferences();
            preferences.setTargetCourse(findCourse(cells[10], where));
            preferences.setMode(parseEnum(StudyMode.class, cells[11], "mode", where));
            preferences.setFormat(parseEnum(GroupFormat.class, cells[12], "format", where));
            preferences.setGoals(parseGoals(cells[13], where));
            preferences.setIntensity(parseEnum(Intensity.class, cells[14], "intensity", where));
            profile.setPreferences(preferences);

            profile.setAvailability(parseAvailability(cells[15], where));
            profile.setLookingFor(cells[16]);

            student.setProfile(profile);
            studentRepository.save(student);
            count++;
        }
        return count;
    }

    /** Reads every line of one seed file, header included. */
    private List<String> readLines(String fileName) throws SeedDataException {
        // Read from the classpath, not a file path: the seed folder is copied into the
        // built app (and into the jar when packaged), while a path like
        // "src/main/resources/seed/..." only exists when running from the project folder.
        InputStream input = getClass().getResourceAsStream("/seed/" + fileName);
        if (input == null) {
            throw new SeedDataException(fileName + ": file not found in src/main/resources/seed/");
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new SeedDataException(fileName + ": could not be read", e);
        }
        return lines;
    }

    /** Splits one row into trimmed cells and checks the column count. */
    private String[] splitRow(String line, int expectedColumns, String where) throws SeedDataException {
        // The -1 keeps empty cells at the END of a row (e.g. an empty last column).
        // Without it, split silently drops them and a valid row looks too short.
        String[] cells = line.split(",", -1);
        if (cells.length != expectedColumns) {
            throw new SeedDataException(where + ": expected " + expectedColumns
                    + " columns, found " + cells.length);
        }
        for (int i = 0; i < cells.length; i++) {
            cells[i] = cells[i].trim();
        }
        return cells;
    }

    /** Looks up a course by code; an unknown code is a seed error. */
    private Course findCourse(String code, String where) throws SeedDataException {
        Course course = courseRepository.findByCode(code);
        if (course == null) {
            throw new SeedDataException(where + ": unknown course code " + code);
        }
        return course;
    }

    /** Turns "IS442|IS215" into a list of courses; an empty cell gives an empty list. */
    private List<Course> parseCourseList(String cell, String where) throws SeedDataException {
        List<Course> courses = new ArrayList<>();
        if (cell.isEmpty()) {
            return courses;
        }
        // split takes a regular expression, where | means "or", so it must be escaped as \\|.
        String[] codes = cell.split("\\|");
        for (String code : codes) {
            courses.add(findCourse(code.trim(), where));
        }
        return courses;
    }

    /** Turns "EXAM_PREP|CONCEPT_REVIEW" into a set of goals; an empty cell gives an empty set. */
    private Set<StudyGoal> parseGoals(String cell, String where) throws SeedDataException {
        Set<StudyGoal> goals = new HashSet<>();
        if (cell.isEmpty()) {
            return goals;
        }
        String[] names = cell.split("\\|");     // escaped pipe, see parseCourseList
        for (String name : names) {
            goals.add(parseEnum(StudyGoal.class, name.trim(), "goal", where));
        }
        return goals;
    }

    /** Converts text such as "ONLINE" to the matching value of the given enum type. */
    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String column, String where)
            throws SeedDataException {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw new SeedDataException(where + ": unknown " + column + " '" + value + "'", e);
        }
    }

    /** Reads the year of study as a whole number. */
    private int parseYear(String cell, String where) throws SeedDataException {
        try {
            return Integer.parseInt(cell);
        } catch (NumberFormatException e) {
            throw new SeedDataException(where + ": year must be a whole number, found '" + cell + "'", e);
        }
    }

    /** Reads the availability cell, e.g. "MON 19-21;WED 19-21". */
    private AvailabilityGrid parseAvailability(String cell, String where) throws SeedDataException {
        try {
            return AvailabilityGrid.parse(cell);
        } catch (IllegalArgumentException e) {
            throw new SeedDataException(where + ": bad availability, " + e.getMessage(), e);
        }
    }
}
