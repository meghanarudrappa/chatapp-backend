package com.chatapp.chatapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "messages")
public class ChatMessage {

    @Id
    @JsonProperty("id") // Ensures React Native "id" maps to MongoDB "_id"
    private String id;

    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
    private MessageStatus status;


    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }

    // Explicitly helping Jackson with the ID if Lombok's @Data is being ignored by the IDE
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
}