package com.chatapp.chatapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupRequest {
    private String name;
    private List<String> participants; // List of phone numbers
    private String groupImage; // Optional


}