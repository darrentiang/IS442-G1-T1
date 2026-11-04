package com.studybuddy.seed;

import com.studybuddy.model.Course;
import com.studybuddy.repository.CourseRepository;

/** Loads courses.csv. Columns: code,title,school. */
public class CourseSeeder implements Seeder {

    /** The seed file this seeder reads. */
    public static final String FILE_NAME = "courses.csv";
    private static final int COLUMNS = 3;

    private final CourseRepository courseRepository;

    /** Creates the seeder with the repository it fills. */
    public CourseSeeder(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /** Returns "courses.csv". */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /** Saves one course per row. */
    @Override
    public int load(CsvFile file) throws SeedDataException {
        int count = 0;
        for (int line : file.getRowLineNumbers()) {
            String[] cells = file.splitRow(line, COLUMNS);
            courseRepository.save(new Course(cells[0], cells[1], cells[2]));
            count++;
        }
        return count;
    }
}
