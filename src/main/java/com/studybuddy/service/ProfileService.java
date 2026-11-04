package com.studybuddy.service;

import org.springframework.stereotype.Service;

import com.studybuddy.model.ProfileView;
import com.studybuddy.model.Student;

/**
 * Decides what one student may see of another's profile. This is where the "contact number
 * hidden until a request is accepted" rule is enforced: toConnectedView() is called only here,
 * and only for an active connection.
 */
@Service
public class ProfileService {

    /** The two students have an active connection. */
    public static final String CONNECTED = "CONNECTED";
    /** The viewer has a PENDING request to the target. */
    public static final String REQUEST_SENT = "REQUEST_SENT";
    /** The target has a PENDING request to the viewer. */
    public static final String REQUEST_RECEIVED = "REQUEST_RECEIVED";
    /** None of the above. */
    public static final String NONE = "NONE";

    private final MatchRequestService requestService;

    /** Creates the service with the request rules it asks about connections (constructor injection). */
    public ProfileService(MatchRequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Returns the target's profile as the viewer may see it: the connected view (with the
     * contact number) only when they have an active connection, otherwise the public view.
     *
     * @param viewer the student looking
     * @param target the student being looked at
     * @return the view, or null if the target has no profile
     */
    public ProfileView viewProfile(Student viewer, Student target) {
        if (target.getProfile() == null) {
            return null;
        }
        if (requestService.isConnected(viewer.getId(), target.getId())) {
            return target.getProfile().toConnectedView();
        }
        return target.getProfile().toPublicView();
    }

    /** Returns how the viewer stands with the target: CONNECTED, REQUEST_SENT, REQUEST_RECEIVED or NONE. */
    public String relationship(Student viewer, Student target) {
        if (requestService.isConnected(viewer.getId(), target.getId())) {
            return CONNECTED;
        }
        if (requestService.hasPendingRequest(viewer.getId(), target.getId())) {
            return REQUEST_SENT;
        }
        if (requestService.hasPendingRequest(target.getId(), viewer.getId())) {
            return REQUEST_RECEIVED;
        }
        return NONE;
    }
}
