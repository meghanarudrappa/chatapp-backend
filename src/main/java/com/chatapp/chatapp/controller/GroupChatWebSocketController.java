package com.chatapp.chatapp.controller;


import com.chatapp.chatapp.model.GroupMessage;
import com.chatapp.chatapp.service.GroupMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Controller
public class GroupChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GroupMessageService groupMessageService;

    @MessageMapping("/group-message")
    public void handleGroupMessage(@Payload GroupMessage message) {
        // 1. Save to MongoDB
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.Instant.now());
        }
        GroupMessage savedMsg = groupMessageService.saveMessage(message);

        // 2. Broadcast to all group members
        // React Native is subscribing to /topic/group/{id}
        String destination = "/topic/group/" + message.getGroupId();
        messagingTemplate.convertAndSend(destination, savedMsg);
    }
}