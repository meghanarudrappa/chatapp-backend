package com.chatapp.chatapp.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceService {

    // Maps WebSocket SessionID -> User Identifier (PhoneNumber/UserId)
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    // Maps User Identifier -> Boolean (True if online)
    private final Map<String, Boolean> activeUsers = new ConcurrentHashMap<>();

    /**
     * Marks a user as Online.
     * Logic: Adds the session mapping and sets the user status to true.
     */
    public void markOnline(String sessionId, String userId) {
        if (sessionId != null && userId != null) {
            sessionToUser.put(sessionId, userId);
            activeUsers.put(userId, true);
        }
    }

    /**
     * Marks a user as Offline.
     * Logic: Removes the specific session. Only sets the user to offline
     * if they have no other active WebSocket sessions (multi-device support).
     * @return the userId that went offline, or null if not found.
     */

    public String markOffline(String sessionId) {
        String userId = sessionToUser.remove(sessionId);

        if (userId != null) {
            // Check if the user has any other active sessions (e.g., Web + Mobile)
            if (!sessionToUser.containsValue(userId)) {
                activeUsers.remove(userId);
            }
        }
        return userId;
    }

    /**
     * Check if a specific user is currently connected.
     */
    public boolean isUserOnline(String userId) {
        return activeUsers.getOrDefault(userId, false);
    }

    /**
     * Returns a snapshot of all currently online users.
     * Useful for the "Contacts" list to show who is available.
     */
    public Map<String, Boolean> getOnlineUsers() {
        return new ConcurrentHashMap<>(activeUsers);
    }
}