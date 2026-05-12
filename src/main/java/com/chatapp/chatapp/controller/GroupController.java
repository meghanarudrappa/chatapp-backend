package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.dto.MessageStatusUpdate;
import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.model.Group;
import com.chatapp.chatapp.model.GroupMessage;
import com.chatapp.chatapp.service.GroupMessageService;
import com.chatapp.chatapp.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*") // Added for React Native connectivity
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable String id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }

        if (group.getCreatorId() != null) {
            group.getMembers().add(group.getCreatorId());
        }

        Group saved = groupService.createGroup(group);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{phoneNumber}")
    public ResponseEntity<List<Group>> getUserGroups(@PathVariable String phoneNumber) {
        List<Group> groups = groupService.getGroupsForUser(phoneNumber);
        return ResponseEntity.ok(groups);
    }

    /**
     * PAGINATION: Fetches messages in chunks.
     * Logic: Page 0 = 20 most recent messages.
     */
    @GetMapping("/messages/group/{groupId}")
    public ResponseEntity<List<GroupMessage>> getGroupMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        long startTime = System.currentTimeMillis();


        // Log this to your IntelliJ console so you can verify the "scroll up" trigger

        List<GroupMessage> messages = groupMessageService.getGroupMessagesPaged(groupId, page, size);

        long endtime = System.currentTimeMillis();
        System.out.println("DEBUG: Found " + messages.size() + " messages for page " + page);
        return ResponseEntity.ok(messages);
    }

    /**
     * TICK LOGIC: Broadcasts 'READ' status to the group.
     */
    @MessageMapping("/group.message.read")
    public void processGroupMessageRead(@Payload MessageStatusUpdate update) {
        System.out.println("DEBUG: Processing READ tick for MsgID: " + update.getMessageId() + " in Group: " + update.getGroupId());

        // Update status in the database
        groupMessageService.updateMessageStatus(
                update.getMessageId(),
                ChatMessage.MessageStatus.READ
        );

        // Broadcast the update to all subscribers of this group
        // The frontend should listen to /topic/group/{groupId}
        messagingTemplate.convertAndSend(
                "/topic/group/" + update.getGroupId(),
                update
        );
    }
}