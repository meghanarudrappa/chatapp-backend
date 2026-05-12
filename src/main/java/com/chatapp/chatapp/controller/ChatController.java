package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.dto.MessageStatusUpdate;
import com.chatapp.chatapp.dto.RecentChatDTO;
import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.service.ChatMessageService;

import com.chatapp.chatapp.service.UserPresenceService;
import com.chatapp.chatapp.websocket.UserSessionManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Data
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    //Used for redis status
    private final UserPresenceService userPresenceService;
    private final UserSessionManager userSessionManager;


    
   
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        log.info("Incoming message from {} to {}", chatMessage.getSenderId(), chatMessage.getRecipientId());

        // 1. Set initial status and timestamp
        chatMessage.setStatus(ChatMessage.MessageStatus.SENT);
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(String.valueOf(new java.util.Date()));
        }

        // 2. Save to MongoDB (Generates the official ID)
        ChatMessage savedMsg = chatMessageService.save(chatMessage);

        // 3. INSTANT PUSH TO RECIPIENT
        // We remove the 'isOnline' check here to ensure the WebSocket always tries.
        // Spring handles the routing.
        log.info("Pushing message {} to recipient queue", savedMsg.getId());
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                savedMsg
        );

        // 4. SYNC BACK TO SENDER
        // This confirms to the sender's UI that the message is now in the DB.
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),
                "/queue/messages",
                savedMsg
        );

        // 5. SEND 'SENT' STATUS UPDATE
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),
                "/queue/status",
                Map.of(
                        "messageId", savedMsg.getId(),
                        "status", "SENT",
                        "recipientId", chatMessage.getRecipientId()
                )
        );
    }

    /**
     * Triggered when the phone receives a message.
     */
    @MessageMapping("/message.delivered")
    public void processMessageDelivered(@Payload Map<String, String> payload) {
        String messageId = payload.get("messageId");
        String senderId = payload.get("senderId"); // The original sender
        String recipientId = payload.get("recipientId"); // The person acknowledging

        if (messageId != null && senderId != null) {
            chatMessageService.updateMessageStatus(messageId, ChatMessage.MessageStatus.DELIVERED);

            // Notify the sender that the message hit the recipient's phone
            messagingTemplate.convertAndSendToUser(
                    senderId,
                    "/queue/status",
                    Map.of(
                            "messageId", messageId,
                            "contactId", recipientId,
                            "status", "DELIVERED"
                    )
            );
        }
    }

    /**
     * Triggered when a user opens a chat screen.
     */
    @MessageMapping("/chat.seen")
    public void markConversationAsSeen(@Payload Map<String, String> payload) {
        String senderId = payload.get("senderId");
        String recipientId = payload.get("recipientId");

        if (senderId != null && recipientId != null) {
            chatMessageService.markAsSeen(senderId, recipientId);

            log.info("Chat between {} and {} marked as READ", senderId, recipientId);

            // Notify the original sender to show Blue Ticks (READ)
            messagingTemplate.convertAndSendToUser(
                    senderId,
                    "/queue/status",
                    Map.of(
                            "contactId", recipientId,
                            "status", "READ"
                    )
            );
        }
    }

    @MessageMapping("/message.read")
    public void processMessageRead(@Payload MessageStatusUpdate update) {
        chatMessageService.updateMessageStatus(update.getMessageId(), ChatMessage.MessageStatus.READ);
        messagingTemplate.convertAndSendToUser(
                update.getSenderId(),
                "/queue/status",
                update
        );
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public List<ChatMessage> findChatMessages(@PathVariable String senderId, @PathVariable String recipientId) {
        return chatMessageService.findChatMessages(senderId, recipientId);
    }

    @GetMapping("/messages/recent/{phoneNumber}")
    public ResponseEntity<List<RecentChatDTO>> getRecentChats(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(chatMessageService.getRecentConversations(phoneNumber));
    }
}