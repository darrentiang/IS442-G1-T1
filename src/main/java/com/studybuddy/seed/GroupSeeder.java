package com.studybuddy.seed;

import java.util.ArrayList;
import java.util.List;

import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudyMode;
import com.studybuddy.model.Visibility;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;

/**
 * Loads groups.csv. Columns: id,name,course,leader,members,max_size,visibility,open,goals,mode,
 * availability,description. The members list includes the leader. Courses and students must
 * already be loaded.
 */
public class GroupSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "groups.csv";
    private static final int COLUMNS = 12;

    private final StudyGroupRepository groupRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    /** Creates the seeder with the repository it fills and the ones it looks courses and students up in. */
    public GroupSeeder(StudyGroupRepository groupRepository, CourseRepository courseRepository,
            StudentRepository studentRepository) {
        this.groupRepository = groupRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /** Returns "groups.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one group with its members per row. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);

            StudyGroup group = new StudyGroup(cells[0]);
            group.setName(cells[1]);
            group.setCourse(file.require(courseRepository.findByCode(cells[2]), "course code", cells[2], line));

            Student leader = findStudent(file, cells[3], line);
            group.setLeader(leader);
            group.setMaxSize(file.parseWholeNumber(cells[5], "max_size", line));     // before adding members
            try {
                for (Student member : parseStudentList(file, cells[4], line)) {
                    group.addMember(member);     // throws if full or listed twice
                }
            } catch (BusinessRuleException e) {
                throw new SeedDataException(file.where(line) + ": " + e.getMessage());
            }
            if (!group.hasMember(leader)) {
                throw new SeedDataException(file.where(line) + ": leader " + cells[3] + " must also be in members");
            }

            group.setVisibility(file.parseEnum(Visibility.class, cells[6], "visibility", line));
            group.setOpen(file.parseBoolean(cells[7], "open", line));
            group.setGoals(file.parseGoals(cells[8], line));
            group.setMode(file.parseEnum(StudyMode.class, cells[9], "mode", line));
            group.setAvailability(file.parseAvailability(cells[10], line));
            group.setDescription(cells[11]);

            groupRepository.save(group);
            count++;
        }
        return count;
    }

    /** Turns "S001|S002" into a list of students; an empty cell gives an empty list. */
    private List<Student> parseStudentList(CsvFile file, String cell, int line) throws SeedDataException {
        List<Student> students = new ArrayList<>();
        for (String id : file.splitList(cell)) {
            students.add(findStudent(file, id, line));
        }
        return students;
    }

    /** Looks up a student by id; an unknown id is a seed error. */
    private Student findStudent(CsvFile file, String id, int line) throws SeedDataException {
        return file.require(studentRepository.findById(id), "student id", id, line);
    }
}
