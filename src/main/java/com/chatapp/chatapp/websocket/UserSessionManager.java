package com.chatapp.chatapp.websocket;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionManager {
    // Maps SessionID -> PhoneNumber (One user can have multiple sessions/devices)
    private final Map<String, String> sessionToPhone = new ConcurrentHashMap<>();

    public void addSession(String sessionId, String phoneNumber) {
        sessionToPhone.put(sessionId, phoneNumber);
    }

    /**
     * Removes the session and returns the phoneNumber associated with it.
     * This allows the EventListener to know WHO went offline.
     */
    public String removeSession(String sessionId) {
        return sessionToPhone.remove(sessionId);
    }

    /**
     * A user is online if their phoneNumber exists as a VALUE in any active session.
     */
    public boolean isOnline(String phoneNumber) {
        return sessionToPhone.containsValue(phoneNumber);
    }
}
