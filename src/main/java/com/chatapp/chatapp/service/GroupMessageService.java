package com.chatapp.chatapp.service;

import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.model.Group;
import com.chatapp.chatapp.model.GroupMessage;
import com.chatapp.chatapp.repository.GroupMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMessageService {

    @Autowired
    private GroupMessageRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public GroupMessage saveMessage(GroupMessage msg) {
        // 1. Set metadata
        msg.setTimestamp(LocalDateTime.now());
        // Default to SENT for the first tick
        if (msg.getStatus() == null) {
            msg.setStatus(ChatMessage.MessageStatus.SENT);
        }

        // 2. Save the message
        GroupMessage savedMsg = repository.save(msg);

        // 3. Update the group metadata for the Inbox view
        updateGroupMetadata(msg.getGroupId(), msg.getContent(), msg.getTimestamp());

        return savedMsg;
    }

    private void updateGroupMetadata(String groupId, String content, LocalDateTime timestamp) {
        Query query = new Query(Criteria.where("id").is(groupId));
        Update update = new Update()
                .set("lastMessage", content)
                .set("lastMessageTimestamp", timestamp);

        mongoTemplate.updateFirst(query, update, Group.class);
    }

    /**
     * NEW: Paged Message Retrieval
     */
    public List<GroupMessage> getGroupMessagesPaged(String groupId, int page, int size) {
        // We sort by timestamp DESCENDING so Page 0 is the most recent 20 messages.
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return repository.findByGroupId(groupId, pageable).getContent();
    }

    /**
     * Legacy method: Keep this if you still have parts of the app
     * that need the full history at once.
     */
    public List<GroupMessage> getGroupMessages(String groupId) {
        return repository.findByGroupIdOrderByTimestampAsc(groupId);
    }

    public void updateMessageStatus(String messageId, ChatMessage.MessageStatus status) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, GroupMessage.class);
    }

    public void markAllGroupMessagesAsRead(String groupId) {
        Query query = new Query(Criteria.where("groupId").is(groupId)
                .and("status").ne(ChatMessage.MessageStatus.READ));

        Update update = new Update().set("status", ChatMessage.MessageStatus.READ);
        mongoTemplate.updateMulti(query, update, GroupMessage.class);
    }
}