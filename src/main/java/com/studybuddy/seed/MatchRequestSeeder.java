package com.studybuddy.seed;

import java.time.LocalDate;

import com.studybuddy.model.Connection;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Loads match-requests.csv. Columns: id,from,to,status,message. From and to must be different
 * known students. An ACCEPTED request also creates the connection between the two students,
 * just as accepting it in the app would. An empty message is stored as null.
 */
public class MatchRequestSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "match-requests.csv";
    private static final int COLUMNS = 5;

    private final MatchRequestRepository matchRequestRepository;
    private final StudentRepository studentRepository;
    private final ConnectionRepository connectionRepository;

    /** Creates the seeder with the repositories it fills and the one it looks students up in. */
    public MatchRequestSeeder(MatchRequestRepository matchRequestRepository, StudentRepository studentRepository,
            ConnectionRepository connectionRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.studentRepository = studentRepository;
        this.connectionRepository = connectionRepository;
    }

    /** Returns "match-requests.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one buddy request per row, plus a connection for each ACCEPTED one. */
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

            MatchRequest request = new MatchRequest(cells[0], from, to);     // starts PENDING
            if (!cells[4].isEmpty()) {
                request.setMessage(cells[4]);
            }
            if (status == RequestStatus.DECLINED) {
                request.decline();
            }
            if (status == RequestStatus.ACCEPTED) {
                request.accept();
                connectionRepository.save(new Connection(connectionRepository.nextId(), from, to, LocalDate.now()));
            }
            matchRequestRepository.save(request);
            count++;
        }
        return count;
    }
}
