package com.studybuddy.seed;

import com.studybuddy.model.JoinRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;

/**
 * Loads join-requests.csv. Columns: id,student,group,status. An ACCEPTED requester must already
 * be in the group's members (groups.csv); a PENDING or DECLINED one must not be. Closed groups
 * have no PENDING requests, because closing a group declines them.
 */
public class JoinRequestSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "join-requests.csv";
    private static final int COLUMNS = 4;

    private final JoinRequestRepository joinRequestRepository;
    private final StudentRepository studentRepository;
    private final StudyGroupRepository groupRepository;

    /** Creates the seeder with the repository it fills and the ones it looks students and groups up in. */
    public JoinRequestSeeder(JoinRequestRepository joinRequestRepository, StudentRepository studentRepository,
            StudyGroupRepository groupRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
    }

    /** Returns "join-requests.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one join request per row, answered as the status column says. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);

            Student student = file.require(studentRepository.findById(cells[1]), "student id", cells[1], line);
            StudyGroup group = file.require(groupRepository.findById(cells[2]), "group id", cells[2], line);
            RequestStatus status = file.parseEnum(RequestStatus.class, cells[3], "status", line);

            boolean isMember = group.hasMember(student);
            if (status == RequestStatus.ACCEPTED && !isMember) {
                throw new SeedDataException(file.where(line) + ": request is ACCEPTED but " + cells[1]
                        + " is not in the members of " + cells[2] + " in groups.csv");
            }
            if (status != RequestStatus.ACCEPTED && isMember) {
                throw new SeedDataException(file.where(line) + ": request is " + status + " but " + cells[1]
                        + " is already a member of " + cells[2]);
            }
            if (status == RequestStatus.PENDING && !group.isOpen()) {
                throw new SeedDataException(file.where(line) + ": " + cells[2]
                        + " is closed, so it cannot have a PENDING request");
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
}
