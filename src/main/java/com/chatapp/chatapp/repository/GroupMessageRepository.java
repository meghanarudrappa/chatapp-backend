package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.model.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMessageRepository extends MongoRepository<GroupMessage, String> {

    /**
     * NEW: Supports Pagination
     * Spring Data will use the Pageable object to determine the sort order,
     * the number of messages to skip, and the limit.
     */
    Page<GroupMessage> findByGroupId(String groupId, Pageable pageable);

    /**
     * Keep this for fetching the entire history if needed.
     */
    List<GroupMessage> findByGroupIdOrderByTimestampAsc(String groupId);

    /**
     * Custom query to update message status (SENT, DELIVERED, READ)
     */
    @Query("{ 'id' : ?0 }")
    @Update("{ '$set' : { 'status' : ?1 } }")
    void updateMessageStatus(String messageId, String status);
}