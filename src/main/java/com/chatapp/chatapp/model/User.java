package com.chatapp.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor  //  Required for JSON parsing and MongoDB
@AllArgsConstructor //  Good practice with Lombok
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String phoneNumber;

    private String displayName; // 🛡️ The "Gatekeeper" field

    private String profileImage;

    private String status = "Available";

    private List<String> contacts = new ArrayList<>();
    // 🚀 You can keep this custom one if you want
    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}