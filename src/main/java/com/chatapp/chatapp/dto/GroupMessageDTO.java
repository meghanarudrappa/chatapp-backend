package com.chatapp.chatapp.dto;

import lombok.Data;

@Data
public class GroupMessageDTO {
    private String id;
    private String groupId;
    private String senderNumber;
    private String senderName;
    private String content;
    private String timestamp;
    private String status;
}
