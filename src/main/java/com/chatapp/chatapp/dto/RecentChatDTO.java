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
public class RecentChatDTO {
    private String contactNumber;
    private String contactName; // Useful for the Inbox list
    private String content;
    private String timestamp;
    private String lastMessageSenderId; // To identify if the user or the contact sent the last msg
    private ChatMessage.MessageStatus status;
    private boolean unread;
}