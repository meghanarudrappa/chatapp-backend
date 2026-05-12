package com.chatapp.chatapp.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String phoneNumber;
    private String otpCode; // used for verify-otp
}
