package com.chatapp.chatapp.repository;


import com.chatapp.chatapp.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {
    // Find groups where the user is a member
    List<Group> findByMembersContaining(String phoneNumber);
}
