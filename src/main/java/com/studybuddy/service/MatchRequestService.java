package com.studybuddy.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.studybuddy.model.AccountStatus;
import com.studybuddy.model.Connection;
import com.studybuddy.model.MatchRequest;
import com.studybuddy.model.RequestAlreadyHandledException;
import com.studybuddy.model.RequestNotAllowedException;
import com.studybuddy.model.RequestStatus;
import com.studybuddy.model.Student;
import com.studybuddy.repository.ConnectionRepository;
import com.studybuddy.repository.MatchRequestRepository;
import com.studybuddy.repository.StudentRepository;

/**
 * Business rules for buddy requests between two students. Shared: Group 1 owns sending
 * (Find Buddies); Group 3 adds the receiving side (accept, decline, connections) here.
 */
@Service
public class MatchRequestService {

    private static final int MAX_MESSAGE_LENGTH = 300;

    private final MatchRequestRepository requestRepository;
    private final ConnectionRepository connectionRepository;
    private final StudentRepository studentRepository;

    /** Creates the service with the repositories it reads and writes (constructor injection). */
    public MatchRequestService(MatchRequestRepository requestRepository,
            ConnectionRepository connectionRepository, StudentRepository studentRepository) {
        this.requestRepository = requestRepository;
        this.connectionRepository = connectionRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Sends a buddy request, after checking every rule.
     *
     * @param from the student sending the request
     * @param toId the id of the student to send it to
     * @param message an optional note of at most 300 characters; blank is stored as null
     * @return the new PENDING request
     * @throws RequestNotAllowedException with a message to show, if a rule refuses the request
     */
    public MatchRequest sendRequest(Student from, String toId, String message) {
        Student to = studentRepository.findById(toId);
        if (to == null || to.getStatus() != AccountStatus.ACTIVE) {
            throw new RequestNotAllowedException("This student isn't available for requests");
        }
        if (to.getId().equals(from.getId())) {
            throw new RequestNotAllowedException("You can't send a request to yourself");
        }
        if (hasPendingRequest(from.getId(), to.getId())) {
            throw new RequestNotAllowedException("Your request to " + to.getName() + " is already pending");
        }
        if (hasPendingRequest(to.getId(), from.getId())) {
            throw new RequestNotAllowedException(to.getName() + " has already sent you a request");
        }
        if (isConnected(from.getId(), to.getId())) {
            throw new RequestNotAllowedException("You're already connected with " + to.getName());
        }
        if (hasDeclinedRequest(from.getId(), to.getId())) {
            throw new RequestNotAllowedException(to.getName() + " declined your earlier request");
        }
        if (message != null && message.trim().length() > MAX_MESSAGE_LENGTH) {
            throw new RequestNotAllowedException("Keep the message to " + MAX_MESSAGE_LENGTH + " characters");
        }
        MatchRequest request = new MatchRequest(requestRepository.nextId(), from, to);
        if (message != null && !message.isBlank()) {
            request.setMessage(message.trim());
        }
        requestRepository.save(request);
        return request;
    }

    /** Returns how many PENDING requests this student has received. */
    public int countPendingTo(String studentId) {
        int count = 0;
        for (MatchRequest request : requestRepository.findTo(studentId)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    /** Returns the PENDING requests this student has received, oldest first. */
    public List<MatchRequest> findPendingTo(String studentId) {
        List<MatchRequest> pending = new ArrayList<>();
        for (MatchRequest request : requestRepository.findTo(studentId)) {
            if (request.getStatus() == RequestStatus.PENDING) {
                pending.add(request);
            }
        }
        return pending;
    }

    /**
     * Accepts a request sent to this student and connects the two students.
     * No second connection is made if they are already connected.
     *
     * @param requestId the request to accept
     * @param studentId the signed-in student, who must be the receiver
     * @return the accepted request
     * @throws RequestNotAllowedException if the request does not exist or was not sent to this student
     * @throws RequestAlreadyHandledException if the request was already accepted or declined
     */
    public MatchRequest accept(String requestId, String studentId) {
        MatchRequest request = findRequestSentTo(requestId, studentId);
        request.accept();
        requestRepository.save(request);
        Student from = request.getFrom();
        Student to = request.getTo();
        if (!isConnected(from.getId(), to.getId())) {
            connectionRepository.save(new Connection(connectionRepository.nextId(), from, to, LocalDate.now()));
        }
        return request;
    }

    /**
     * Declines a request sent to this student. No connection is made.
     *
     * @param requestId the request to decline
     * @param studentId the signed-in student, who must be the receiver
     * @return the declined request
     * @throws RequestNotAllowedException if the request does not exist or was not sent to this student
     * @throws RequestAlreadyHandledException if the request was already accepted or declined
     */
    public MatchRequest decline(String requestId, String studentId) {
        MatchRequest request = findRequestSentTo(requestId, studentId);
        request.decline();
        requestRepository.save(request);
        return request;
    }

    /** Returns the request if it exists and was sent to this student; throws otherwise. */
    private MatchRequest findRequestSentTo(String requestId, String studentId) {
        MatchRequest request = requestRepository.findById(requestId);
        if (request == null || !request.getTo().getId().equals(studentId)) {
            throw new RequestNotAllowedException("This request isn't available");
        }
        return request;
    }

    /** Returns true if a PENDING request from the first student to the second exists. */
    public boolean hasPendingRequest(String fromId, String toId) {
        for (MatchRequest request : requestRepository.findFrom(fromId)) {
            if (request.getTo().getId().equals(toId) && request.getStatus() == RequestStatus.PENDING) {
                return true;
            }
        }
        return false;
    }

    /** Returns true if the second student declined a request from the first one before. */
    public boolean hasDeclinedRequest(String fromId, String toId) {
        for (MatchRequest request : requestRepository.findFrom(fromId)) {
            if (request.getTo().getId().equals(toId) && request.getStatus() == RequestStatus.DECLINED) {
                return true;
            }
        }
        return false;
    }

    /** Returns true if the two students have an active connection. */
    public boolean isConnected(String studentId, String otherId) {
        for (Connection connection : connectionRepository.findActiveFor(studentId)) {
            if (connection.getA().getId().equals(otherId) || connection.getB().getId().equals(otherId)) {
                return true;
            }
        }
        return false;
    }
}
