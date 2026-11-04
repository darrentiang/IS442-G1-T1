# Timetable import

How a student turns their class timetable into availability. Code: `com.studybuddy.importer` (plain Java) and `TimetableImportService`; page: My profile → Edit availability.

## The flow: upload, draft, prune, save

1. **Upload.** The student exports their timetable as an `.ics` calendar file and uploads it on the Edit availability page (`POST /profile/availability/import`, field `timetable`, at most 1 MB, set by `spring.servlet.multipart.max-file-size`).
2. **Draft.** `TimetableImportService` gives the file to the first `TimetableImporter` whose `supports(fileName)` is true. The service never checks the file type itself; each importer decides (polymorphism through an interface). The importer returns a `TimetableImport`: the busy grid, the class names, how many weekly slots, and warnings. `draftFrom` then ticks every 08:00-23:00 hour that is not a class.
3. **Prune.** The same page comes back (no redirect) with the draft: class hours greyed out, marked "class" and disabled; every other hour ticked. A banner says "Found N weekly classes: ..." and lists any warnings.
4. **Save.** The student unticks whatever else they are busy with and clicks Save, which posts the grid like any manual edit. Until then nothing has changed.

A file that cannot be read (wrong type, empty, no events, a bad date, too large) re-shows the page with a red banner and the saved grid untouched. Errors are a checked `TimetableImportException`, so the controller has to handle them.

## Parsing rules (`IcsTimetableImporter`)

- **Unfold first.** RFC 5545 folds long lines: a line starting with a space or tab continues the line before it, minus that one character. Without this, a long `SUMMARY` is cut in half and the second half looks like a broken property.
- **Events only.** Only `BEGIN:VEVENT` ... `END:VEVENT` blocks count; `SUMMARY`, `DTSTART` and `DTEND` are read from each. A property name can carry parameters (`DTSTART;TZID=Asia/Singapore:20260826T153000`): the value is after the first `:`, the name before the first `;`.
- **Time zones.** `yyyyMMdd'T'HHmmss` ending in `Z` is UTC and is converted to Singapore time. A `TZID` parameter or no zone at all is taken as Singapore time already.
- **All-day events** (`VALUE=DATE`, e.g. "Recess Week") are skipped with a warning.
- **Rounding and clipping.** Every hour the event touches is busy: start rounded down, end rounded up, so 15:30-18:45 marks 15, 16, 17 and 18. Hours outside 08:00-23:00 are clipped; an event entirely outside adds a warning.
- **Recurrence collapses.** A class repeats every week, so the same weekday and time on different dates is one slot. `classCount` counts distinct (day, start, end) slots; class names are distinct, in first-seen order.
- **Failures.** No `VEVENT` at all: "No classes found in this file. Is it an .ics calendar export?". A date that cannot be read: the message names the line number.

`java.time` is used in this class only, because calendar files carry real dates and zones. The grid stays `boolean[7][15]`.

`src/main/resources/samples/sample-timetable.ics` exercises every rule: 4 weekly classes over 3 weeks (12 events) using `TZID`, UTC `Z` and zone-less times, one folded `SUMMARY`, and an all-day Recess Week.

## Why the import only drafts

A timetable knows when classes are. It cannot know about CCAs, part-time work, travel or the evenings a student keeps free on purpose, and "not in class" is not the same as "free to study". Matching trusts the grid, so the student asserts their own availability: the import saves typing, the student decides.
