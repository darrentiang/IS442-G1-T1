package com.studybuddy.seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.repository.CourseRepository;
import com.studybuddy.repository.JoinRequestRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.MatchingConfigRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.SystemAdministratorRepository;

/**
 * Loads the demo data from the CSV files in src/main/resources/seed/ once, when the app
 * starts, by running one Seeder per file. Any bad row stops the app from starting, with a
 * message naming the file and line.
 */
@Component
public class SeedDataLoader implements CommandLineRunner {

    private final List<Seeder> seeders = new ArrayList<>();
    private final MatchingConfigRepository matchingConfigRepository;

    /**
     * Creates the seeders in load order: courses, admins, students, groups, join requests,
     * match requests, then the matching configuration. Each file refers to the ones loaded
     * before it (students name courses, groups name students, requests name groups and students).
     */
    public SeedDataLoader(CourseRepository courseRepository,
            SystemAdministratorRepository adminRepository,
            StudentRepository studentRepository,
            StudyGroupRepository groupRepository,
            JoinRequestRepository joinRequestRepository,
            MatchRequestRepository matchRequestRepository,
            MatchingConfigRepository matchingConfigRepository) {
        this.matchingConfigRepository = matchingConfigRepository;
        seeders.add(new CourseSeeder(courseRepository));
        seeders.add(new AdminSeeder(adminRepository));
        seeders.add(new StudentSeeder(studentRepository, courseRepository));
        seeders.add(new GroupSeeder(groupRepository, courseRepository, studentRepository));
        seeders.add(new JoinRequestSeeder(joinRequestRepository, studentRepository, groupRepository));
        seeders.add(new MatchRequestSeeder(matchRequestRepository, studentRepository));
        seeders.add(new MatchingConfigSeeder(matchingConfigRepository));
    }

    /** Runs every seeder in order, then prints one summary line. */
    @Override
    public void run(String... args) throws SeedDataException {
        Map<String, Integer> counts = new HashMap<>();
        for (Seeder seeder : seeders) {
            counts.put(seeder.getFileName(), seeder.load(new CsvFile(seeder.getFileName())));
        }
        int admins = counts.get(AdminSeeder.FILE_NAME);
        String adminWord = (admins == 1) ? " admin, " : " admins, ";
        MatchingConfig config = matchingConfigRepository.get();
        System.out.println("Seeded " + counts.get(CourseSeeder.FILE_NAME) + " courses, " + admins + adminWord
                + counts.get(StudentSeeder.FILE_NAME) + " students, "
                + counts.get(GroupSeeder.FILE_NAME) + " groups, "
                + counts.get(JoinRequestSeeder.FILE_NAME) + " join requests, "
                + counts.get(MatchRequestSeeder.FILE_NAME) + " match requests, "
                + "1 matching config (" + config.getActiveStrategyName() + ", "
                + config.getEnabledCriteria().size() + " criteria on)");
    }
}
