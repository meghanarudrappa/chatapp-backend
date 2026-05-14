package com.chatapp.chatapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map; // ADD THIS IMPORT

@Document(collection = "group_messages")
@CompoundIndex(name = "group_timestamp_idx", def = "{'groupId': 1, 'timestamp': -1}")
@Data
public class GroupMessage {
    @Id
    private String id;
    private String groupId;
    private String senderNumber;
    private String senderName;
    private String content;

    // ADD THIS FIELD TO STORE SIDE DATA
    private Map<String, String> metadata;

    private Instant timestamp = Instant.now();

    private ChatMessage.MessageStatus status = ChatMessage.MessageStatus.SENT;
}