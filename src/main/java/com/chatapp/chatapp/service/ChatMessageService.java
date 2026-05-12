package com.chatapp.chatapp.service;

import com.chatapp.chatapp.dto.RecentChatDTO;
import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final MongoTemplate mongoTemplate;

    /**
     * 1. Save Message
     * Logic: Sets status to SENT and adds timestamp if missing.
     */
    public ChatMessage save(ChatMessage chatMessage) {
        if (chatMessage.getStatus() == null) {
            chatMessage.setStatus(ChatMessage.MessageStatus.SENT);
        }
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return repository.save(chatMessage);
    }

    /**
     * 2. Find History
     * Logic: Returns full conversation history between two users.
     */
    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        List<ChatMessage> history = repository.findChatHistory(senderId, recipientId);
        if (history != null) {
            history.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
            return history;
        }
        return Collections.emptyList();
    }

    /**
     * 3. Update Single Message Status (Individual Ticks)
     */
    public void updateMessageStatus(String messageId, ChatMessage.MessageStatus status) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, ChatMessage.class);
    }

    /**
     * 4. Bulk Mark as Seen (Turns messages into Blue Ticks)
     * Target: Messages sent by the contact to the current user.
     */
    public void markAsSeen(String senderId, String recipientId) {
        Query query = new Query(Criteria.where("senderId").is(senderId)
                .and("recipientId").is(recipientId)
                .and("status").ne(ChatMessage.MessageStatus.READ));

        Update update = new Update().set("status", ChatMessage.MessageStatus.READ);
        mongoTemplate.updateMulti(query, update, ChatMessage.class);
    }

    /**
     * 5. Logic for the Inbox (Recent Conversations)
     */
    public List<RecentChatDTO> getRecentConversations(String phoneNumber) {
        List<RecentChatDTO> recentChats = repository.findRecentChats(phoneNumber);

        for (RecentChatDTO dto : recentChats) {
            if (dto.getStatus() != null) {
                // If the message is NOT from the user AND status is not READ, mark as unread
                // Logic assumes the 'senderId' in DTO represents the last message sender
                boolean isRead = ChatMessage.MessageStatus.READ.toString().equals(dto.getStatus());
                dto.setUnread(!isRead);
            }
        }
        return recentChats;
    }

    /**
     * 6. Find Pending Messages (For "Catch-up" when user comes online)
     */
    public List<ChatMessage> findPendingMessages(String recipientId) {
        return repository.findByRecipientIdAndStatus(recipientId, ChatMessage.MessageStatus.SENT);
    }
}