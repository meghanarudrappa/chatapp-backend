package com.chatapp.chatapp.service;

//This acts as a "plug." If you want to remove Redis, you just change the implementation.
public interface PresenceService {
    boolean isUserOnline(String userId);
    void setUserOnline(String userId);
    void setUserOffline(String userId);
}
