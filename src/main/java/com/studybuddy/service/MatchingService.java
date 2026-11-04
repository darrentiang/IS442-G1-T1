package com.studybuddy.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.studybuddy.engine.MatchContext;
import com.studybuddy.engine.MatchResult;
import com.studybuddy.engine.MatchingConfig;
import com.studybuddy.engine.MatchingEngine;
import com.studybuddy.model.Connection;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.Pass;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.model.StudyGoal;
import com.studybuddy.model.StudyMode;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.PassRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Runs Find Buddies: turns a search into a MatchContext (who to leave out, who is busy) and
 * asks the matching engine for ranked results. Also records passes.
 */
@Service
public class MatchingService {

    private final MatchingEngine engine = MatchingEngine.createDefault();
    /**
     * The settings in use. They move to the admin's MatchingConfigRepository when Group 3's
     * config branch is merged; getConfig() is the single access point, so only that method changes then.
     */
    private final MatchingConfig config = MatchingConfig.defaults();

    private final StudentRepository studentRepository;
    private final MatchRequestRepository requestRepository;
    private final ConnectionRepository connectionRepository;
    private final PassRepository passRepository;

    /** Creates the service with the repositories it reads and writes (constructor injection). */
    public MatchingService(StudentRepository studentRepository, MatchRequestRepository requestRepository,
            ConnectionRepository connectionRepository, PassRepository passRepository) {
        this.studentRepository = studentRepository;
        this.requestRepository = requestRepository;
        this.connectionRepository = connectionRepository;
        this.passRepository = passRepository;
    }

    /**
     * Returns the matching settings in use. The single access point: when the admin's
     * MatchingConfigRepository arrives, only this method changes.
     */
    public MatchingConfig getConfig() {
        return config;
    }

    /** When the search names neither a course nor a goal, searches the seeker's own target course. */
    public void applyDefaults(Student seeker, BuddySearch search) {
        if (!isBlank(search.getCourseCode()) || !isBlank(search.getGoal())) {
            return;
        }
        if (seeker.getProfile() != null && seeker.getProfile().getPreferences() != null
                && seeker.getProfile().getPreferences().getTargetCourse() != null) {
            search.setCourseCode(seeker.getProfile().getPreferences().getTargetCourse().getCode());
        }
    }

    /**
     * Finds ranked study buddies for the seeker. Leaves out active connections, anyone with a
     * PENDING request either way, anyone who declined the seeker before, and anyone passed on.
     *
     * @param seeker the student looking for a buddy
     * @param search the search bar values; blank means any
     * @return the results, best first
     */
    public List<MatchResult> findBuddies(Student seeker, BuddySearch search) {
        MatchContext context = new MatchContext(blankToNull(search.getCourseCode()), parseGoal(search.getGoal()),
                parseMode(search.getMode()), search.getMinHours(), excludedIds(seeker), pendingIncoming());
        return engine.findMatches(seeker, studentRepository.findAll(), context, getConfig());
    }

    /** Scores one pair for a candidate's profile page; null if either has no profile. */
    public MatchResult evaluate(Student seeker, Student candidate) {
        return engine.evaluate(seeker, candidate, getConfig());
    }

    /**
     * Hides the candidate from the seeker's future results. Passing twice on the same person
     * changes nothing.
     *
     * @throws IllegalStateException if the candidate is unknown or is the seeker
     */
    public void pass(Student seeker, String candidateId) {
        Student candidate = studentRepository.findById(candidateId);
        if (candidate == null || candidate.getId().equals(seeker.getId())) {
            throw new IllegalStateException("This student could not be found");
        }
        for (Pass pass : passRepository.findBySeeker(seeker.getId())) {
            if (pass.getCandidate().getId().equals(candidateId)) {
                return;
            }
        }
        passRepository.save(new Pass(seeker, candidate, LocalDate.now()));
    }

    /** Puts a passed-on candidate back in the seeker's results; returns false if there was no pass. */
    public boolean undoPass(Student seeker, String candidateId) {
        return passRepository.delete(seeker.getId(), candidateId);
    }

    /** Returns the display name of the strategy in use, e.g. "Balanced". */
    public String getStrategyLabel() {
        return engine.getStrategyLabel(getConfig().getActiveStrategyName());
    }

    /** Returns criterion name to display name, e.g. "availability" to "Availability", in engine order. */
    public Map<String, String> getCriterionLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String name : engine.getCriterionNames()) {
            labels.put(name, engine.getCriterionLabel(name));
        }
        return labels;
    }

    /** Returns the ids of everyone the seeker should not be offered. */
    private Set<String> excludedIds(Student seeker) {
        Set<String> excluded = new HashSet<>();
        String seekerId = seeker.getId();
        for (Connection connection : connectionRepository.findActiveFor(seekerId)) {
            excluded.add(connection.getA().getId());
            excluded.add(connection.getB().getId());
        }
        for (MatchRequest request : requestRepository.findFrom(seekerId)) {
            if (request.getStatus() == RequestStatus.PENDING || request.getStatus() == RequestStatus.DECLINED) {
                excluded.add(request.getTo().getId());
            }
        }
        for (MatchRequest request : requestRepository.findTo(seekerId)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                excluded.add(request.getFrom().getId());
            }
        }
        for (Pass pass : passRepository.findBySeeker(seekerId)) {
            excluded.add(pass.getCandidate().getId());
        }
        excluded.remove(seekerId);     // SelfFilter already handles the seeker
        return excluded;
    }

    /** Returns student id to the number of PENDING requests they have received. */
    private Map<String, Integer> pendingIncoming() {
        Map<String, Integer> counts = new HashMap<>();
        for (MatchRequest request : requestRepository.findAll()) {
            if (request.getStatus() == RequestStatus.PENDING) {
                String toId = request.getTo().getId();
                Integer count = counts.get(toId);
                counts.put(toId, (count == null) ? 1 : count + 1);
            }
        }
        return counts;
    }

    /** Returns the goal with this name, or null when blank or unknown. */
    private StudyGoal parseGoal(String name) {
        for (StudyGoal goal : StudyGoal.values()) {
            if (goal.name().equals(name)) {
                return goal;
            }
        }
        return null;
    }

    /** Returns the mode with this name, or null when blank or unknown. */
    private StudyMode parseMode(String name) {
        for (StudyMode mode : StudyMode.values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return null;
    }

    /** Returns the trimmed text, or null when it is null or blank. */
    private String blankToNull(String text) {
        if (isBlank(text)) {
            return null;
        }
        return text.trim();
    }

    /** Returns true if the text is null or only spaces. */
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
