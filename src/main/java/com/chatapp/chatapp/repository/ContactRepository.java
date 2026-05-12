package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContactRepository extends MongoRepository<Contact, String> {
    List<Contact> findByOwnerPhoneNumber(String ownerPhoneNumber);

    // Check if a user already added this specific contact number
    boolean existsByOwnerPhoneNumberAndContactPhone(String ownerPhone, String contactPhone);
}
