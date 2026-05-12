package com.chatapp.chatapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "contacts")
@Data
public class Contact {
    @Id
    private String id;
    private String ownerPhoneNumber; // The user who added the contact
    private String contactName;      // The display name
    private String contactPhone;     // The number to chat with
    private LocalDateTime createdAt = LocalDateTime.now();
}
