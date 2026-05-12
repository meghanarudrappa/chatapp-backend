package com.chatapp.chatapp.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String phoneNumber;
    private String displayName;
    private String profileImage;
    private String status;
}