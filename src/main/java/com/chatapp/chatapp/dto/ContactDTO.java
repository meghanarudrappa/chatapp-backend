package com.chatapp.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactDTO {
    private String id;
    private String name;
    private String phoneNumber;
    private boolean onApp; // The magic flag
}
