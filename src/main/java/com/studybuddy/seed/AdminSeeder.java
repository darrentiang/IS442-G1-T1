package com.studybuddy.seed;

import com.studybuddy.model.SystemAdministrator;
import com.studybuddy.repository.SystemAdministratorRepository;

/** Loads admins.csv. Columns: id,name,email. */
public class AdminSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "admins.csv";
    private static final int COLUMNS = 3;

    private final SystemAdministratorRepository adminRepository;

    /** Creates the seeder with the repository it fills. */
    public AdminSeeder(SystemAdministratorRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /** Returns "admins.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one administrator per row. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);
            adminRepository.save(new SystemAdministrator(cells[0], cells[1], cells[2]));
            count++;
        }
        return count;
    }
}
