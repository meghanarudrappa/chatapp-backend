package com.chatapp.chatapp.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "otp_requests")
public class OtpRequest {
    @Id
    private String id;

    @Indexed(unique = true)
    private String phoneNumber;

    private String otpCode;

    // IMPORTANT: Use java.time.LocalDateTime for better compatibility with modern Spring Data
    @Indexed(expireAfterSeconds = 300)
    private java.time.LocalDateTime createdAt;

    public OtpRequest(String phoneNumber, String otpCode) {
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.createdAt = java.time.LocalDateTime.now();
    }
}