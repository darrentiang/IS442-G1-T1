package com.studybuddy.service;

import com.studybuddy.model.ActivityType;
import com.studybuddy.model.AvailabilityGrid;
import com.studybuddy.model.BusinessRuleException;
import com.studybuddy.model.Connection;
import com.studybuddy.model.Course;
import com.studybuddy.model.Rsvp;
import com.studybuddy.model.SessionDetails;
import com.studybuddy.model.SessionStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.StudySession;
import com.studybuddy.model.TimeBlock;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.SessionDetailsRepository;
import com.studybuddy.repository.StudySessionRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The rules for study sessions: suggesting times everyone is free, proposing a session with a
 * buddy or a study group, answering Going or Can't, confirming, cancelling and marking it done.
 * Every refusal is a BusinessRuleException with a message written for people.
 */
@Service
public class SessionService {

    private final StudySessionRepository sessionRepository;
    private final SessionDetailsRepository detailsRepository;
    private final ConnectionRepository connectionRepository;
    private final StudyGroupService groupService;
    private final UserService userService;
    private final MatchingConfigService configService;
    private final ActivityFeedService feedService;
    private final int maxLocationLength;
    private final int maxSuggestions;

    /** Creates the service with what it needs (constructor injection); limits come from application.properties. */
    public SessionService(StudySessionRepository sessionRepository, SessionDetailsRepository detailsRepository,
            ConnectionRepository connectionRepository, StudyGroupService groupService, UserService userService,
            MatchingConfigService configService, ActivityFeedService feedService, Environment environment) {
        this.sessionRepository = sessionRepository;
        this.detailsRepository = detailsRepository;
        this.connectionRepository = connectionRepository;
        this.groupService = groupService;
        this.userService = userService;
        this.configService = configService;
        this.feedService = feedService;
        this.maxLocationLength = environment.getRequiredProperty("studybuddy.session.max-location-length", Integer.class);
        this.maxSuggestions = environment.getRequiredProperty("studybuddy.session.max-suggestions", Integer.class);
    }

    // ----- Planning -----

    /**
     * Returns everyone a planned session would include: the proposer and their buddy, or every
     * current member of the group (leader first).
     *
     * @throws BusinessRuleException if the form names neither a buddy nor a group, or one the proposer can't plan with
     */
    public List<Student> participantsFor(SessionForm form, Student proposer) {
        if (!isBlank(form.getBuddy())) {
            Connection connection = connectionRepository.findById(form.getBuddy());
            if (connection == null || !connection.isActive() || !isPartOf(connection, proposer.getId())) {
                throw new BusinessRuleException("You can only plan a session with a current buddy");
            }
            List<Student> people = new ArrayList<>();
            people.add(proposer);
            people.add(otherSide(connection, proposer.getId()));
            return people;
        }
        if (!isBlank(form.getGroup())) {
            StudyGroup group = memberGroup(form.getGroup(), proposer.getId());
            if (!group.isOpen()) {
                throw new BusinessRuleException("This group is closed, so it can't plan sessions");
            }
            List<Student> people = groupPeople(group);
            if (people.size() < 2) {
                throw new BusinessRuleException("This group needs at least one other member before planning a session");
            }
            return people;
        }
        throw new BusinessRuleException("Choose a buddy or a group to plan a session with");
    }

    /** Returns who the session is with, as the proposer would say it: the buddy's name or the group's name. */
    public String targetLabel(SessionForm form, Student proposer) {
        if (!isBlank(form.getGroup())) {
            return memberGroup(form.getGroup(), proposer.getId()).getName();
        }
        List<Student> people = participantsFor(form, proposer);
        return people.get(1).getName();
    }

    /** Returns the courses the session can be for: the group's course, or any course the proposer takes. */
    public List<Course> courseOptions(SessionForm form, Student proposer) {
        List<Course> options = new ArrayList<>();
        if (!isBlank(form.getGroup())) {
            options.add(memberGroup(form.getGroup(), proposer.getId()).getCourse());
            return options;
        }
        options.addAll(proposer.getProfile().getCoursesTaken());
        Course target = proposer.getProfile().getPreferences() == null
                ? null : proposer.getProfile().getPreferences().getTargetCourse();
        if (target != null && !containsCourse(options, target.getCode())) {
            options.add(0, target);
        }
        return options;
    }

    /**
     * Returns weekly blocks when every one of these people is free, at least the admin's minimum
     * session length long, in day then hour order, up to the configured number of suggestions.
     */
    public List<TimeBlock> suggestTimes(List<Student> people) {
        List<TimeBlock> suggestions = new ArrayList<>();
        if (people.isEmpty()) {
            return suggestions;
        }
        AvailabilityGrid everyoneFree = new AvailabilityGrid();
        for (int day = 0; day < AvailabilityGrid.DAYS; day++) {
            for (int hour = 0; hour < AvailabilityGrid.HOURS_PER_DAY; hour++) {
                everyoneFree.set(day, hour, isEveryoneFree(people, day, hour));
            }
        }
        int minHours = configService.getConfig().getMinSessionHours();
        for (TimeBlock block : everyoneFree.commonBlocks(everyoneFree, minHours)) {
            if (suggestions.size() >= maxSuggestions) {
                break;
            }
            suggestions.add(block);
        }
        return suggestions;
    }

    /** Returns the admin's minimum session length in hours, used to pre-fill a suggested time. */
    public int minSessionHours() {
        return configService.getConfig().getMinSessionHours();
    }

    /**
     * Checks the form's course, day, times and location. Who the session is with is checked by
     * participantsFor, which the caller runs first.
     *
     * @return field name to message; empty when everything is valid
     */
    public Map<String, String> validate(SessionForm form, Student proposer) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (isBlank(form.getCourseCode()) || !containsCourse(courseOptions(form, proposer), form.getCourseCode())) {
            errors.put("courseCode", "Choose a course");
        }
        Integer day = parseNumber(form.getDay());
        if (day == null || day < 0 || day >= AvailabilityGrid.DAYS) {
            errors.put("day", "Choose a day");
        }
        Integer start = parseNumber(form.getStartHour());
        Integer end = parseNumber(form.getEndHour());
        if (start == null || start < AvailabilityGrid.FIRST_HOUR || start >= AvailabilityGrid.LAST_HOUR) {
            errors.put("startHour", "Choose a start time between " + clock(AvailabilityGrid.FIRST_HOUR)
                    + " and " + clock(AvailabilityGrid.LAST_HOUR - 1));
        }
        if (end == null || end <= AvailabilityGrid.FIRST_HOUR || end > AvailabilityGrid.LAST_HOUR) {
            errors.put("endHour", "Choose an end time up to " + clock(AvailabilityGrid.LAST_HOUR));
        } else if (start != null && end <= start) {
            errors.put("endHour", "The end time must be after the start time");
        }
        String location = form.getLocation() == null ? "" : form.getLocation().trim();
        if (location.isEmpty()) {
            errors.put("location", "Say where you'll meet, such as a library room or an online link");
        } else if (location.length() > maxLocationLength) {
            errors.put("location", "Keep the location under " + maxLocationLength + " characters");
        }
        return errors;
    }

    /**
     * Creates a proposed session from a valid form. The proposer is marked Going; a group session
     * also adds a row to the group's activity feed.
     *
     * @throws BusinessRuleException if the form is not valid or names someone the proposer can't plan with
     */
    public StudySession propose(SessionForm form, Student proposer) {
        List<Student> people = participantsFor(form, proposer);
        if (!validate(form, proposer).isEmpty()) {
            throw new BusinessRuleException("Check the session details and try again");
        }
        StudySession session = new StudySession(sessionRepository.nextId());
        session.setCourse(findCourse(courseOptions(form, proposer), form.getCourseCode()));
        session.setDay(parseNumber(form.getDay()));
        session.setStartHour(parseNumber(form.getStartHour()) - AvailabilityGrid.FIRST_HOUR);
        session.setEndHour(parseNumber(form.getEndHour()) - AvailabilityGrid.FIRST_HOUR);
        session.setLocation(form.getLocation().trim());
        session.setParticipants(people);
        Map<Student, Rsvp> answers = new HashMap<>();
        answers.put(proposer, Rsvp.GOING);
        session.setRsvps(answers);
        sessionRepository.save(session);

        String groupId = isBlank(form.getGroup()) ? null : form.getGroup();
        detailsRepository.save(new SessionDetails(session.getId(), proposer.getId(), groupId));
        if (groupId != null) {
            feedService.record(groupService.findById(groupId), ActivityType.SESSION_PROPOSED,
                    proposer.getName() + " proposed a session for " + session.getCourse().getCode() + ": "
                            + whenOf(session) + " at " + session.getLocation());
        }
        return session;
    }

    // ----- Answering and managing -----

    /**
     * Records a participant's answer, Going or Can't. It can be changed until the session is done.
     *
     * @param answer "GOING" or "CANT"
     * @throws BusinessRuleException if the answer is unknown, the session is done, or the student isn't invited
     */
    public StudySession answer(String sessionId, String studentId, String answer) {
        Rsvp rsvp = parseAnswer(answer);
        StudySession session = participantSession(sessionId, studentId);
        if (session.getStatus() == SessionStatus.PAST) {
            throw new BusinessRuleException("This session is already done");
        }
        Map<Student, Rsvp> answers = new HashMap<>(session.getRsvps());
        Student existing = participantById(session, studentId);
        answers.remove(existing);
        answers.put(existing, rsvp);
        session.setRsvps(answers);
        sessionRepository.save(session);
        return session;
    }

    /**
     * Confirms a proposed session. Only its proposer can, once at least one other participant is going.
     *
     * @throws BusinessRuleException if the student didn't propose it, it isn't proposed, or nobody else is going yet
     */
    public StudySession confirm(String sessionId, String studentId) {
        StudySession session = proposedBy(sessionId, studentId);
        if (session.getStatus() != SessionStatus.PROPOSED) {
            throw new BusinessRuleException("Only a proposed session can be confirmed");
        }
        if (!someoneElseGoing(session, studentId)) {
            throw new BusinessRuleException("Wait until at least one other person says they're going");
        }
        session.setStatus(SessionStatus.CONFIRMED);
        sessionRepository.save(session);
        SessionDetails details = detailsRepository.findBySession(sessionId);
        if (details != null && details.isGroupSession()) {
            feedService.record(groupService.findById(details.getGroupId()), ActivityType.SESSION_CONFIRMED,
                    "The " + session.getCourse().getCode() + " session on " + whenOf(session) + " is confirmed");
        }
        return session;
    }

    /**
     * Marks a confirmed session as done, moving it to Past. Only its proposer can.
     *
     * @throws BusinessRuleException if the student didn't propose it or it isn't confirmed
     */
    public StudySession markDone(String sessionId, String studentId) {
        StudySession session = proposedBy(sessionId, studentId);
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new BusinessRuleException("Only a confirmed session can be marked as done");
        }
        session.setStatus(SessionStatus.PAST);
        sessionRepository.save(session);
        return session;
    }

    /**
     * Cancels a session that hasn't happened yet, removing it for everyone. Only its proposer can.
     *
     * @throws BusinessRuleException if the student didn't propose it or it is already done
     */
    public StudySession cancel(String sessionId, String studentId) {
        StudySession session = proposedBy(sessionId, studentId);
        if (session.getStatus() == SessionStatus.PAST) {
            throw new BusinessRuleException("A session that's already done can't be cancelled");
        }
        sessionRepository.delete(sessionId);
        detailsRepository.deleteBySession(sessionId);
        return session;
    }

    // ----- Reading -----

    /** Returns this student's sessions that haven't happened yet, through the week. */
    public List<SessionRow> upcomingFor(String studentId) {
        return rows(sessionRepository.findByParticipant(studentId), studentId, true);
    }

    /** Returns this student's sessions that are done, through the week. */
    public List<SessionRow> pastFor(String studentId) {
        return rows(sessionRepository.findByParticipant(studentId), studentId, false);
    }

    /** Returns a group's sessions as the viewing member sees them; upcoming when upcoming is true, otherwise done ones. */
    public List<SessionRow> forGroup(String groupId, String viewerId, boolean upcoming) {
        List<StudySession> sessions = new ArrayList<>();
        for (SessionDetails details : detailsRepository.findByGroup(groupId)) {
            StudySession session = sessionRepository.findById(details.getSessionId());
            if (session != null) {
                sessions.add(session);
            }
        }
        return rows(sessions, viewerId, upcoming);
    }

    /**
     * Returns the group if the student is in it.
     *
     * @throws BusinessRuleException if there is no such group or the student is not a member
     */
    public StudyGroup memberGroup(String groupId, String studentId) {
        StudyGroup group = groupService.findById(groupId);
        if (group == null || !groupService.isMember(group, studentId)) {
            throw new BusinessRuleException("Only members of this group can see or plan its sessions");
        }
        return group;
    }

    /** Returns the group a session was planned for, or null for a buddy session or an unknown session. */
    public String groupIdOf(String sessionId) {
        SessionDetails details = detailsRepository.findBySession(sessionId);
        return details == null ? null : details.getGroupId();
    }

    /** Returns a session's day and time as text, such as "WED 19:00–21:00". */
    public String whenOf(StudySession session) {
        return AvailabilityGrid.dayName(session.getDay()) + " "
                + clock(AvailabilityGrid.FIRST_HOUR + session.getStartHour()) + "–"
                + clock(AvailabilityGrid.FIRST_HOUR + session.getEndHour());
    }

    // ----- Helpers -----

    /** Builds page rows for the sessions that match the upcoming flag, ordered through the week. */
    private List<SessionRow> rows(List<StudySession> sessions, String viewerId, boolean upcoming) {
        List<StudySession> chosen = new ArrayList<>();
        for (StudySession session : sessions) {
            boolean isUpcoming = session.getStatus() != SessionStatus.PAST;
            if (isUpcoming == upcoming) {
                chosen.add(session);
            }
        }
        chosen.sort(new SessionTimeComparator());
        List<SessionRow> rows = new ArrayList<>();
        for (StudySession session : chosen) {
            rows.add(toRow(session, viewerId));
        }
        return rows;
    }

    /** Builds one row; participants whose accounts no longer exist are left out. */
    private SessionRow toRow(StudySession session, String viewerId) {
        SessionDetails details = detailsRepository.findBySession(session.getId());
        String proposerId = details == null ? null : details.getProposerId();
        List<String> going = new ArrayList<>();
        List<String> cant = new ArrayList<>();
        List<String> waiting = new ArrayList<>();
        List<String> others = new ArrayList<>();
        Rsvp myAnswer = null;
        boolean participant = false;
        for (Student person : session.getParticipants()) {
            if (userService.findStudent(person.getId()) == null) {
                continue;
            }
            Rsvp answer = answerOf(session, person.getId());
            if (person.getId().equals(viewerId)) {
                participant = true;
                myAnswer = answer;
            } else {
                others.add(person.getName());
            }
            if (answer == Rsvp.GOING) {
                going.add(person.getName());
            } else if (answer == Rsvp.CANT) {
                cant.add(person.getName());
            } else {
                waiting.add(person.getName());
            }
        }
        String withLabel;
        if (details != null && details.isGroupSession()) {
            StudyGroup group = groupService.findById(details.getGroupId());
            withLabel = group == null ? "A study group" : group.getName();
        } else {
            withLabel = others.isEmpty() ? "Your buddy" : others.get(0);
        }
        Student proposerStudent = proposerId == null ? null : userService.findStudent(proposerId);
        String proposerName = proposerStudent == null ? "A former member" : proposerStudent.getName();
        boolean proposer = viewerId != null && viewerId.equals(proposerId);
        boolean confirmable = proposer && session.getStatus() == SessionStatus.PROPOSED
                && someoneElseGoing(session, viewerId);
        return new SessionRow(session, whenOf(session), withLabel, proposerName, going, cant, waiting,
                myAnswer, participant, proposer, confirmable);
    }

    /** Returns the session if the student is one of its participants. */
    private StudySession participantSession(String sessionId, String studentId) {
        StudySession session = sessionRepository.findById(sessionId);
        if (session == null || participantById(session, studentId) == null) {
            throw new BusinessRuleException("This session isn't available");
        }
        return session;
    }

    /** Returns the session if the student proposed it. */
    private StudySession proposedBy(String sessionId, String studentId) {
        StudySession session = participantSession(sessionId, studentId);
        SessionDetails details = detailsRepository.findBySession(sessionId);
        if (details == null || !details.getProposerId().equals(studentId)) {
            throw new BusinessRuleException("Only the person who proposed this session can do that");
        }
        return session;
    }

    /** Returns true if a participant other than this student, whose account still exists, is going. */
    private boolean someoneElseGoing(StudySession session, String studentId) {
        for (Student person : session.getParticipants()) {
            if (!person.getId().equals(studentId) && userService.findStudent(person.getId()) != null
                    && answerOf(session, person.getId()) == Rsvp.GOING) {
                return true;
            }
        }
        return false;
    }

    /** Returns this participant's answer, or null if they have not answered. */
    private Rsvp answerOf(StudySession session, String studentId) {
        for (Map.Entry<Student, Rsvp> entry : session.getRsvps().entrySet()) {
            if (entry.getKey().getId().equals(studentId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** Returns the participant with this id, or null if they are not in the session. */
    private Student participantById(StudySession session, String studentId) {
        for (Student person : session.getParticipants()) {
            if (person.getId().equals(studentId)) {
                return person;
            }
        }
        return null;
    }

    /** Returns the group's leader and members once each, leader first. */
    private List<Student> groupPeople(StudyGroup group) {
        List<Student> people = new ArrayList<>();
        if (group.getLeader() != null) {
            people.add(group.getLeader());
        }
        for (Student member : group.getMembers()) {
            if (group.getLeader() == null || !member.getId().equals(group.getLeader().getId())) {
                people.add(member);
            }
        }
        return people;
    }

    /** Returns true if every one of these people is free at this hour. */
    private boolean isEveryoneFree(List<Student> people, int day, int hour) {
        for (Student person : people) {
            AvailabilityGrid grid = person.getProfile().getAvailability();
            if (grid == null || !grid.isAvailable(day, hour)) {
                return false;
            }
        }
        return true;
    }

    /** Turns "GOING" or "CANT" into an answer. */
    private Rsvp parseAnswer(String answer) {
        if ("GOING".equals(answer)) {
            return Rsvp.GOING;
        }
        if ("CANT".equals(answer)) {
            return Rsvp.CANT;
        }
        throw new BusinessRuleException("Choose Going or Can't");
    }

    /** Returns true if this student is on either side of the connection. */
    private boolean isPartOf(Connection connection, String studentId) {
        return connection.getA().getId().equals(studentId) || connection.getB().getId().equals(studentId);
    }

    /** Returns the student on the other side of the connection. */
    private Student otherSide(Connection connection, String studentId) {
        return connection.getA().getId().equals(studentId) ? connection.getB() : connection.getA();
    }

    /** Returns true if the list has a course with this code. */
    private boolean containsCourse(List<Course> courses, String code) {
        return findCourse(courses, code) != null;
    }

    /** Returns the course with this code from the list, or null. */
    private Course findCourse(List<Course> courses, String code) {
        for (Course course : courses) {
            if (course != null && course.getCode().equals(code)) {
                return course;
            }
        }
        return null;
    }

    /** Reads a whole number from text, or returns null if it isn't one. */
    private Integer parseNumber(String text) {
        if (isBlank(text)) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns a clock hour as text, such as "19:00". */
    private String clock(int hour) {
        return String.format("%02d:00", hour);
    }

    /** Returns true for null or blank text. */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
