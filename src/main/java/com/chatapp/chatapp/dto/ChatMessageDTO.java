package com.chatapp.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;//sent as a string to the phone
}