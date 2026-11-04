package com.studybuddy.seed;

import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Loads match-requests.csv. Columns: id,from,to,status,message. From and to must be different
 * known students. Only PENDING and DECLINED are allowed: an ACCEPTED request needs a connection,
 * which is created by accepting a request in the app. An empty message is stored as null.
 */
public class MatchRequestSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "match-requests.csv";
    private static final int COLUMNS = 5;

    private final MatchRequestRepository matchRequestRepository;
    private final StudentRepository studentRepository;

    /** Creates the seeder with the repository it fills and the one it looks students up in. */
    public MatchRequestSeeder(MatchRequestRepository matchRequestRepository, StudentRepository studentRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.studentRepository = studentRepository;
    }

    /** Returns "match-requests.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one buddy request per row. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);

            Student from = file.require(studentRepository.findById(cells[1]), "student id", cells[1], line);
            Student to = file.require(studentRepository.findById(cells[2]), "student id", cells[2], line);
            if (from.equals(to)) {
                throw new SeedDataException(file.where(line) + ": from and to are both " + cells[1]);
            }
            RequestStatus status = file.parseEnum(RequestStatus.class, cells[3], "status", line);
            if (status == RequestStatus.ACCEPTED) {
                throw new SeedDataException(file.where(line) + ": status must be PENDING or DECLINED, found ACCEPTED");
            }

            MatchRequest request = new MatchRequest(cells[0], from, to);     // starts PENDING
            if (!cells[4].isEmpty()) {
                request.setMessage(cells[4]);
            }
            if (status == RequestStatus.DECLINED) {
                request.decline();
            }
            matchRequestRepository.save(request);
            count++;
        }
        return count;
    }
}
