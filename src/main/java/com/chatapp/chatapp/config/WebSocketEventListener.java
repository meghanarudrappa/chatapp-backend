package com.chatapp.chatapp.config;

import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.service.ChatMessageService;
import com.chatapp.chatapp.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent; // Changed from SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    /**
     * Use SessionConnectedEvent (instead of ConnectEvent) to ensure the
     * STOMP connection is fully established before sending catch-up messages.
     */
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Retrieve phone number from the user principal set in the Interceptor
        String phoneNumber = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        if (phoneNumber != null && sessionId != null) {
            log.info("User fully connected: {} [Session: {}]", phoneNumber, sessionId);

            presenceService.markOnline(sessionId, phoneNumber);

            // 1. Alert others that this user is now online
            messagingTemplate.convertAndSend("/topic/user-status/" + phoneNumber, Map.of("online", true));

            // 2. Catch-up: Push pending messages stored in DB while offline
            List<ChatMessage> pendingMessages = chatMessageService.findPendingMessages(phoneNumber);
            if (!pendingMessages.isEmpty()) {
                log.info("Pushing {} missed messages to {}", pendingMessages.size(), phoneNumber);
                pendingMessages.forEach(msg ->
                        messagingTemplate.convertAndSendToUser(phoneNumber, "/queue/messages", msg)
                );
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            String phoneNumber = presenceService.markOffline(sessionId);
            if (phoneNumber != null) {
                log.info("User offline: {}", phoneNumber);
                // Broadcast Offline Status
                messagingTemplate.convertAndSend("/topic/user-status/" + phoneNumber, Map.of("online", false));
            }
        }
    }

    /**
     * Handles the "WhatsApp feel": When User A opens a chat with User B,
     * they subscribe to User B's status. This method catches that subscription
     * and immediately tells User A if User B is currently online.
     */
    @EventListener
    public void handleSubscriptionEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/user-status/")) {
            String targetPhoneNumber = destination.replace("/topic/user-status/", "");
            boolean isOnline = presenceService.isUserOnline(targetPhoneNumber);

            log.debug("Status check: {} is {}", targetPhoneNumber, isOnline ? "ONLINE" : "OFFLINE");

            // We use the specific destination of the subscription to reply only to the requester
            messagingTemplate.convertAndSend(destination, Map.of("online", isOnline));
        }
    }
}