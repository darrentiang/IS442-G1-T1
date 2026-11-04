package com.studybuddy.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.StudyGoal;

/**
 * One seed CSV file read from src/main/resources/seed/: its data rows by line number, and the
 * cell parsers every seeder shares. Every error message names the file and the line.
 */
public class CsvFile {

    private static final String SEED_FOLDER = "/seed/";
    private static final String LIST_SEPARATOR = "\\|";     // split takes a regular expression, so | is escaped

    private final String fileName;
    private final List<String> lines;

    /** Reads every line of one seed file, header included; throws if it is missing or unreadable. */
    public CsvFile(String fileName) throws SeedDataException {
        this.fileName = fileName;
        this.lines = readLines(fileName);
    }

    /** Returns the file name, e.g. "students.csv". */
    public String getFileName() {
        return fileName;
    }

    /** Returns the line numbers of the data rows: every non-blank line after the header, in file order. */
    public List<Integer> getRowLineNumbers() {
        List<Integer> rowLines = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {     // line 1 (index 0) is the header
            if (!lines.get(i).isBlank()) {
                rowLines.add(i + 1);
            }
        }
        return rowLines;
    }

    /** Returns where a problem is, for error messages: "students.csv line 14". */
    public String where(int line) {
        return fileName + " line " + line;
    }

    /** Splits the row on this line into trimmed cells and checks the column count. */
    public String[] splitRow(int line, int expectedColumns) throws SeedDataException {
        // The -1 keeps empty cells at the END of a row (e.g. an empty last column).
        // Without it, split silently drops them and a valid row looks too short.
        String[] cells = lines.get(line - 1).split(",", -1);
        if (cells.length != expectedColumns) {
            throw new SeedDataException(where(line) + ": expected " + expectedColumns
                    + " columns, found " + cells.length);
        }
        for (int i = 0; i < cells.length; i++) {
            cells[i] = cells[i].trim();
        }
        return cells;
    }

    /** Splits a list cell such as "IS442|IS215" into trimmed items; an empty cell gives no items. */
    public List<String> splitList(String cell) {
        List<String> items = new ArrayList<>();
        if (cell.isEmpty()) {
            return items;
        }
        for (String item : cell.split(LIST_SEPARATOR)) {
            items.add(item.trim());
        }
        return items;
    }

    /** Returns the object a lookup found, or throws "unknown [what] [id]" when the lookup found nothing. */
    public <T> T require(T found, String what, String id, int line) throws SeedDataException {
        if (found == null) {
            throw new SeedDataException(where(line) + ": unknown " + what + " " + id);
        }
        return found;
    }

    /** Converts text such as "ONLINE" to the matching value of the given enum type. */
    public <E extends Enum<E>> E parseEnum(Class<E> type, String value, String column, int line)
            throws SeedDataException {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw new SeedDataException(where(line) + ": unknown " + column + " '" + value + "'", e);
        }
    }

    /** Turns "EXAM_PREP|CONCEPT_REVIEW" into a set of goals in declaration order; an empty cell gives an empty set. */
    public Set<StudyGoal> parseGoals(String cell, int line) throws SeedDataException {
        Set<StudyGoal> goals = EnumSet.noneOf(StudyGoal.class);
        for (String name : splitList(cell)) {
            goals.add(parseEnum(StudyGoal.class, name, "goal", line));
        }
        return goals;
    }

    /** Reads a cell as a whole number, such as a year or a group's max_size. */
    public int parseWholeNumber(String cell, String column, int line) throws SeedDataException {
        try {
            return Integer.parseInt(cell);
        } catch (NumberFormatException e) {
            throw new SeedDataException(where(line) + ": " + column + " must be a whole number, found '" + cell + "'", e);
        }
    }

    /** Reads a cell as a number with an optional decimal point, such as a config's minimum_score. */
    public double parseNumber(String cell, String column, int line) throws SeedDataException {
        try {
            return Double.parseDouble(cell);
        } catch (NumberFormatException e) {
            throw new SeedDataException(where(line) + ": " + column + " must be a number, found '" + cell + "'", e);
        }
    }

    /** Reads a cell that must be exactly "true" or "false". */
    public boolean parseBoolean(String cell, String column, int line) throws SeedDataException {
        if (cell.equals("true")) {
            return true;
        }
        if (cell.equals("false")) {
            return false;
        }
        throw new SeedDataException(where(line) + ": " + column + " must be true or false, found '" + cell + "'");
    }

    /** Reads an availability cell, e.g. "MON 19-21;WED 19-21". */
    public AvailabilityGrid parseAvailability(String cell, int line) throws SeedDataException {
        try {
            return AvailabilityGrid.parse(cell);
        } catch (IllegalArgumentException e) {
            throw new SeedDataException(where(line) + ": bad availability, " + e.getMessage(), e);
        }
    }

    /** Reads every line of the file from the classpath. */
    private static List<String> readLines(String fileName) throws SeedDataException {
        // Read from the classpath, not a file path: the seed folder is copied into the
        // built app (and into the jar when packaged), while a path like
        // "src/main/resources/seed/..." only exists when running from the project folder.
        InputStream input = CsvFile.class.getResourceAsStream(SEED_FOLDER + fileName);
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
}
