package com.skill_learning_platform.skill_learning_platform.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionAccessService {

    public void requireStudent(HttpSession session, Long studentId) {
        require(session, "studentId", studentId);
    }

    public void requireMentor(HttpSession session, Long mentorId) {
        require(session, "mentorId", mentorId);
    }

    public void requireAdmin(HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin login is required.");
        }
    }

    public void requireSignedIn(HttpSession session) {
        if (session.getAttribute("studentId") == null
                && session.getAttribute("mentorId") == null
                && session.getAttribute("adminId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
    }

    private void require(HttpSession session, String attribute, Long requestedId) {
        Object authenticatedId = session.getAttribute(attribute);
        if (authenticatedId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
        if (requestedId == null || !authenticatedId.toString().equals(requestedId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's data.");
        }
    }
}