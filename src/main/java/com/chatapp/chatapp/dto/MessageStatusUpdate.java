package com.chatapp.chatapp.dto;

import com.chatapp.chatapp.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageStatusUpdate {
    private String messageId;
    private String senderId;    // The person who originally sent the message
    private String recipientId; // Used for 1-1 chats
    private String groupId;     // REQUIRED for Group Chats to broadcast ticks
    private ChatMessage.MessageStatus status; // Usually DELIVERED or READ
}