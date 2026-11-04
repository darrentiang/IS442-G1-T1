package com.studybuddy.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.studybuddy.model.AvailabilityGrid;

/** What a timetable import found: the hours in class, the class names, how many weekly slots, and warnings. Immutable. */
public class TimetableImport {

    private static final int DAYS = 7;
    private static final int HOURS = 15;

    private final AvailabilityGrid busy;
    private final List<String> classNames;
    private final int classCount;
    private final List<String> warnings;

    /** Creates the result, keeping private copies of the grid and the lists. */
    public TimetableImport(AvailabilityGrid busy, List<String> classNames, int classCount, List<String> warnings) {
        this.busy = copyOf(busy);
        this.classNames = Collections.unmodifiableList(new ArrayList<>(classNames));
        this.classCount = classCount;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    /** Returns a copy of the grid where true means "in class" at that hour. */
    public AvailabilityGrid getBusy() {
        return copyOf(busy);
    }

    /** Returns the distinct class names, in the order they first appeared in the file. */
    public List<String> getClassNames() {
        return classNames;
    }

    /** Returns the number of distinct weekly class slots (same weekday and time counts once). */
    public int getClassCount() {
        return classCount;
    }

    /** Returns notes about entries that were skipped or clipped; empty if none. */
    public List<String> getWarnings() {
        return warnings;
    }

    /** Copies a grid cell by cell, so nobody outside can change this result. */
    private static AvailabilityGrid copyOf(AvailabilityGrid grid) {
        AvailabilityGrid copy = new AvailabilityGrid();
        for (int day = 0; day < DAYS; day++) {
            for (int hour = 0; hour < HOURS; hour++) {
                copy.set(day, hour, grid.isAvailable(day, hour));
            }
        }
        return copy;
    }
}
