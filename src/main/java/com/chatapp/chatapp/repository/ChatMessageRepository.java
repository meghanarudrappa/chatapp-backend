package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.model.ChatMessage;
import com.chatapp.chatapp.dto.RecentChatDTO;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Fetches the 1-1 conversation history between two users.
     */
    @Query("{ '$or': [ { 'senderId': ?0, 'recipientId': ?1 }, { 'senderId': ?1, 'recipientId': ?0 } ] }")
    List<ChatMessage> findChatHistory(String user1, String user2);

    /**
     * Complex Aggregation to generate the Inbox/Recent Chats list.
     * Logic:
     * 1. Match messages involving the user.
     * 2. Sort by newest first.
     * 3. Group by the 'other' person in the conversation.
     * 4. Project into RecentChatDTO.
     */
    @Aggregation(pipeline = {
            "{ '$match': { '$or': [ { 'senderId': ?0 }, { 'recipientId': ?0 } ] } }",
            "{ '$sort': { 'timestamp': -1 } }",
            "{ '$group': { " +
                    "    '_id': { " +
                    "        '$cond': [ " +
                    "            { '$eq': [ '$senderId', ?0 ] }, " +
                    "            '$recipientId', " +
                    "            '$senderId' " +
                    "        ] " +
                    "    }, " +
                    "    'content': { '$first': '$content' }, " +
                    "    'timestamp': { '$first': '$timestamp' }, " +
                    "    'status': { '$first': '$status' }, " +
                    "    'lastMessageSenderId': { '$first': '$senderId' } " +
                    "} }",
            "{ '$sort': { 'timestamp': -1 } }",
            "{ '$project': { " +
                    "    'contactNumber': '$_id', " +
                    "    'content': 1, " +
                    "    'timestamp': 1, " +
                    "    'status': 1, " +
                    "    'lastMessageSenderId': 1, " +
                    "    '_id': 0 " +
                    "} }"
    })
    List<RecentChatDTO> findRecentChats(String phoneNumber);

    List<ChatMessage> findByRecipientIdAndStatus(String recipientId, ChatMessage.MessageStatus status);
}