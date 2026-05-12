package com.chatapp.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptDTO {
    private String messageId;
    private String senderId;   // The person who sent the original message
    private String recipientId; // The person who just read it
    private String groupId;    // Null for 1-1 chats
    private String status;     // e.g., "READ"
}