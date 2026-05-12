package com.chatapp.chatapp.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
}
