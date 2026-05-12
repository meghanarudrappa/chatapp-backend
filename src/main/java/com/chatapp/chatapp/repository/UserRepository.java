package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    // 🚀 THE FIX: Add this line for the search functionality
    List<User> findByDisplayNameContainingIgnoreCase(String displayName);

    // ADD THIS LINE:
    List<User> findByPhoneNumberIn(List<String> phoneNumbers);
    List<User> findByPhoneNumberContaining(String query);

    boolean existsByPhoneNumber(String phoneNumber);
}