package com.chatapp.chatapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import java.util.Set;

@Document(collection = "groups")
@Data
public class Group {
    @Id
    private String id;
    private String name;
    private String groupImage;
    private String creatorId; // The phone number of who made it

    private Set<String> members; // List of phone numbers
    private LocalDateTime createdAt = LocalDateTime.now();
    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;
}
