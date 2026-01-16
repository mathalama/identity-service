package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.SessionInfo;
import dev.mathalama.identityservice.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    public static final String USER_ID_ATTR = "userId";
    public static final String USERNAME_ATTR = "username";
    public static final String EMAIL_ATTR = "email";

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    /**
     * Creates a new session for the authenticated user
     */
    public void createSession(HttpServletRequest request, Users user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID_ATTR, user.getId());
        session.setAttribute(USERNAME_ATTR, user.getUsername());
        session.setAttribute(EMAIL_ATTR, user.getEmail());
        session.setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                user.getUsername()
        );
        log.info("Session created for user: {} with sessionId: {}", user.getUsername(), session.getId());
    }

    /**
     * Invalidates the current session
     */
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute(USERNAME_ATTR);
            session.invalidate();
            log.info("Session invalidated for user: {}", username);
        }
    }

    /**
     * Gets the current session info
     */
    public SessionInfo getCurrentSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return new SessionInfo(
                session.getId(),
                (Long) session.getAttribute(USER_ID_ATTR),
                (String) session.getAttribute(USERNAME_ATTR),
                (String) session.getAttribute(EMAIL_ATTR),
                Instant.ofEpochMilli(session.getCreationTime()),
                Instant.ofEpochMilli(session.getLastAccessedTime()),
                session.getMaxInactiveInterval()
        );
    }

    /**
     * Checks if the current request has an active session
     */
    public boolean hasActiveSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_ID_ATTR) != null;
    }

    /**
     * Gets the current user ID from session
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Long) session.getAttribute(USER_ID_ATTR);
        }
        return null;
    }

    /**
     * Gets the current username from session
     */
    public String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute(USERNAME_ATTR);
        }
        return null;
    }

    /**
     * Gets all active sessions for a specific user (by username)
     */
    public List<SessionInfo> getUserSessions(String username) {
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
        
        return sessions.values().stream()
                .map(session -> new SessionInfo(
                        session.getId(),
                        session.getAttribute(USER_ID_ATTR),
                        session.getAttribute(USERNAME_ATTR),
                        session.getAttribute(EMAIL_ATTR),
                        session.getCreationTime(),
                        session.getLastAccessedTime(),
                        (int) session.getMaxInactiveInterval().toSeconds()
                ))
                .toList();
    }

    /**
     * Invalidates all sessions for a specific user
     */
    public void invalidateAllUserSessions(String username) {
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
        sessions.keySet().forEach(sessionRepository::deleteById);
        log.info("All sessions invalidated for user: {}", username);
    }

    /**
     * Invalidates a specific session by ID
     */
    public void invalidateSessionById(String sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Session invalidated: {}", sessionId);
    }
}
