package com.studybuddy.seed;

/** Loads one seed CSV file into the repositories. One class per file; SeedDataLoader runs them in order. */
public interface Seeder {

    /** Returns the seed file this seeder reads, e.g. "courses.csv". */
    String getFileName();

    /** Saves one object per row of the file; returns how many rows were loaded. Throws on the first bad row. */
    int load(CsvFile file) throws SeedDataException;
}
