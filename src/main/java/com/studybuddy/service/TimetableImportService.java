package com.studybuddy.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.importer.IcsTimetableImporter;
import com.studybuddy.importer.TimetableImport;
import com.studybuddy.importer.TimetableImportException;
import com.studybuddy.importer.TimetableImporter;
import com.studybuddy.model.AvailabilityGrid;

/**
 * Turns an uploaded timetable into a DRAFT availability grid. The import only drafts: a
 * timetable cannot know about CCAs, work or travel, so the student prunes the draft and
 * decides what to save. Nothing here saves anything.
 */
@Service
public class TimetableImportService {

    private static final int DAYS = 7;
    private static final int HOURS = 15;
    private static final String UNSUPPORTED_MESSAGE = "Upload an .ics calendar file";

    private final List<TimetableImporter> importers = new ArrayList<>();

    /** Creates the service with every importer it knows (B3b adds the screenshot importer here). */
    public TimetableImportService() {
        importers.add(new IcsTimetableImporter());
    }

    /**
     * Reads the file with the first importer that supports its name. The service never checks
     * the file type itself; each importer decides (polymorphism).
     *
     * @param fileName the uploaded file's original name
     * @param input the file's contents
     * @return what the importer found
     * @throws TimetableImportException if no importer supports the file or the file is unreadable
     */
    public TimetableImport importTimetable(String fileName, InputStream input) throws TimetableImportException {
        for (TimetableImporter importer : importers) {
            if (importer.supports(fileName)) {
                return importer.read(input);
            }
        }
        throw new TimetableImportException(UNSUPPORTED_MESSAGE);
    }

    /** Returns a draft grid: every 08:00-23:00 hour NOT in class is ticked as available. */
    public AvailabilityGrid draftFrom(TimetableImport imported) {
        AvailabilityGrid busy = imported.getBusy();
        AvailabilityGrid draft = new AvailabilityGrid();
        for (int day = 0; day < DAYS; day++) {
            for (int hour = 0; hour < HOURS; hour++) {
                draft.set(day, hour, !busy.isAvailable(day, hour));
            }
        }
        return draft;
    }
}
