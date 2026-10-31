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
import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudentProfile;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.StudyPreferences;
import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.model.Visibility;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
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
    private static final String GROUPS_FILE = "groups.csv";
    private static final String JOIN_REQUESTS_FILE = "join-requests.csv";

    private static final int COURSE_COLUMNS = 3;
    private static final int ADMIN_COLUMNS = 3;
    private static final int STUDENT_COLUMNS = 17;
    private static final int GROUP_COLUMNS = 12;
    private static final int JOIN_REQUEST_COLUMNS = 4;

    private final CourseRepository courseRepository;
    private final SystemAdministratorRepository adminRepository;
    private final StudentRepository studentRepository;
    private final StudyGroupRepository groupRepository;
    private final JoinRequestRepository joinRequestRepository;

    /** Creates the loader with the repositories it fills (constructor injection). */
    public SeedDataLoader(CourseRepository courseRepository,
            SystemAdministratorRepository adminRepository,
            StudentRepository studentRepository,
            StudyGroupRepository groupRepository,
            JoinRequestRepository joinRequestRepository) {
        this.courseRepository = courseRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    /**
     * Loads courses, admins, students, groups, then join requests. Each file refers to the
     * ones loaded before it (students name courses, groups name students, requests name groups).
     */
    @Override
    public void run(String... args) throws SeedDataException {
        int courses = loadCourses();
        int admins = loadAdmins();
        int students = loadStudents();
        int groups = loadGroups();
        int joinRequests = loadJoinRequests();
        String adminWord = (admins == 1) ? " admin, " : " admins, ";
        System.out.println("Seeded " + courses + " courses, " + admins + adminWord + students + " students, "
                + groups + " groups, " + joinRequests + " join requests");
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

    /**
     * Columns: id,name,course,leader,members,max_size,visibility,open,goals,mode,
     * availability,description. The members list includes the leader.
     */
    private int loadGroups() throws SeedDataException {
        List<String> lines = readLines(GROUPS_FILE);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            String where = GROUPS_FILE + " line " + (i + 1);
            String[] cells = splitRow(lines.get(i), GROUP_COLUMNS, where);

            StudyGroup group = new StudyGroup(cells[0]);
            group.setName(cells[1]);
            group.setCourse(findCourse(cells[2], where));

            Student leader = findStudent(cells[3], where);
            group.setLeader(leader);
            group.setMaxSize(parseWholeNumber(cells[5], "max_size", where));     // before adding members
            try {
                for (Student member : parseStudentList(cells[4], where)) {
                    group.addMember(member);     // throws if full or listed twice
                }
            } catch (IllegalStateException e) {
                throw new SeedDataException(where + ": " + e.getMessage());
            }
            if (!group.hasMember(leader)) {
                throw new SeedDataException(where + ": leader " + cells[3] + " must also be in members");
            }

            group.setVisibility(parseEnum(Visibility.class, cells[6], "visibility", where));
            group.setOpen(parseBoolean(cells[7], "open", where));
            group.setGoals(parseGoals(cells[8], where));
            group.setMode(parseEnum(StudyMode.class, cells[9], "mode", where));
            group.setAvailability(parseAvailability(cells[10], where));
            group.setDescription(cells[11]);

            groupRepository.save(group);
            count++;
        }
        return count;
    }

    /**
     * Columns: id,student,group,status. An ACCEPTED requester must already be in the group's
     * members (groups.csv); a PENDING or DECLINED one must not be. Closed groups have no
     * PENDING requests, because closing a group declines them.
     */
    private int loadJoinRequests() throws SeedDataException {
        List<String> lines = readLines(JOIN_REQUESTS_FILE);
        int count = 0;
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (lines.get(i).isBlank()) {
                continue;
            }
            String where = JOIN_REQUESTS_FILE + " line " + (i + 1);
            String[] cells = splitRow(lines.get(i), JOIN_REQUEST_COLUMNS, where);

            Student student = findStudent(cells[1], where);
            StudyGroup group = findGroup(cells[2], where);
            RequestStatus status = parseEnum(RequestStatus.class, cells[3], "status", where);

            boolean isMember = group.hasMember(student);
            if (status == RequestStatus.ACCEPTED && !isMember) {
                throw new SeedDataException(where + ": request is ACCEPTED but " + cells[1]
                        + " is not in the members of " + cells[2] + " in groups.csv");
            }
            if (status != RequestStatus.ACCEPTED && isMember) {
                throw new SeedDataException(where + ": request is " + status + " but " + cells[1]
                        + " is already a member of " + cells[2]);
            }
            if (status == RequestStatus.PENDING && !group.isOpen()) {
                throw new SeedDataException(where + ": " + cells[2] + " is closed, so it cannot have a PENDING request");
            }

            JoinRequest request = new JoinRequest(cells[0], student, group);     // starts PENDING
            if (status == RequestStatus.ACCEPTED) {
                request.accept();     // the member is already in the group from groups.csv
            } else if (status == RequestStatus.DECLINED) {
                request.decline();
            }
            joinRequestRepository.save(request);
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

    /** Looks up a student by id; an unknown id is a seed error. */
    private Student findStudent(String id, String where) throws SeedDataException {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new SeedDataException(where + ": unknown student id " + id);
        }
        return student;
    }

    /** Looks up a group by id; an unknown id is a seed error. */
    private StudyGroup findGroup(String id, String where) throws SeedDataException {
        StudyGroup group = groupRepository.findById(id);
        if (group == null) {
            throw new SeedDataException(where + ": unknown group id " + id);
        }
        return group;
    }

    /** Turns "S001|S002" into a list of students; an empty cell gives an empty list. */
    private List<Student> parseStudentList(String cell, String where) throws SeedDataException {
        List<Student> students = new ArrayList<>();
        if (cell.isEmpty()) {
            return students;
        }
        String[] ids = cell.split("\\|");     // escaped pipe, see parseCourseList
        for (String id : ids) {
            students.add(findStudent(id.trim(), where));
        }
        return students;
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

    /** Reads a cell as a whole number, such as a group's max_size. */
    private int parseWholeNumber(String cell, String column, String where) throws SeedDataException {
        try {
            return Integer.parseInt(cell);
        } catch (NumberFormatException e) {
            throw new SeedDataException(where + ": " + column + " must be a whole number, found '" + cell + "'", e);
        }
    }

    /** Reads a cell that must be exactly "true" or "false". */
    private boolean parseBoolean(String cell, String column, String where) throws SeedDataException {
        if (cell.equals("true")) {
            return true;
        }
        if (cell.equals("false")) {
            return false;
        }
        throw new SeedDataException(where + ": " + column + " must be true or false, found '" + cell + "'");
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
