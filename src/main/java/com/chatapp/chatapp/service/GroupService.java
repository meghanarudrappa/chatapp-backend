package com.chatapp.chatapp.service;

import com.chatapp.chatapp.model.GroupMessage;
import com.chatapp.chatapp.model.Group;
import com.chatapp.chatapp.repository.GroupMessageRepository;
import com.chatapp.chatapp.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMessageRepository messageRepository;

    // CHANGED: Now accepts the Group object directly to fix the "Required type: String" error
    public Group createGroup(Group group) {
        // 1. Generate a UUID if the frontend didn't provide one
        if (group.getId() == null || group.getId().isEmpty()) {
            group.setId(UUID.randomUUID().toString());
        }

        // 2. Ensure members Set is initialized to prevent NullPointerException
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }

        // 3. Ensure the creator is always included in the members list
        if (group.getCreatorId() != null) {
            group.getMembers().add(group.getCreatorId());
        }

        // 4. Set the creation timestamp
        group.setCreatedAt(LocalDateTime.now());

        return groupRepository.save(group);
    }

    public List<Group> getGroupsForUser(String phoneNumber) {
        return groupRepository.findByMembersContaining(phoneNumber);
    }

    public GroupMessage saveMessage(GroupMessage message) {
        return messageRepository.save(message);
    }

    public List<GroupMessage> getMessagesForGroup(String groupId) {
        return messageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }

    public Optional<Group> getGroupById(String id) {
        return groupRepository.findById(id);
    }
}