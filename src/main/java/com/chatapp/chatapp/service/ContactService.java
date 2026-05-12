package com.chatapp.chatapp.service;

import com.chatapp.chatapp.dto.ContactDTO;
import com.chatapp.chatapp.model.Contact;
import com.chatapp.chatapp.repository.ContactRepository;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public ContactDTO saveContact(Contact contact) {
        // 1. Prevent adding yourself
        if (contact.getOwnerPhoneNumber().equals(contact.getContactPhone())) {
            throw new RuntimeException("You cannot add yourself as a contact");
        }

        // 2. Prevent duplicates (check if this owner already added this phone)
        List<Contact> existing = contactRepository.findByOwnerPhoneNumber(contact.getOwnerPhoneNumber());
        boolean alreadyExists = existing.stream()
                .anyMatch(c -> c.getContactPhone().equals(contact.getContactPhone()));

        if (alreadyExists) {
            Contact existingContact = existing.stream()
                    .filter(c -> c.getContactPhone().equals(contact.getContactPhone()))
                    .findFirst().get();
            return convertToDTO(existingContact);
        }

        // 3. Save mapping
        Contact saved = contactRepository.save(contact);
        return convertToDTO(saved);
    }

    public List<ContactDTO> getAllMyContacts(String ownerPhone) {
        List<Contact> contacts = contactRepository.findByOwnerPhoneNumber(ownerPhone);
        return contacts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ContactDTO convertToDTO(Contact contact) {
        // Just the simple boolean discovery check
        boolean isRegistered = userRepository.existsByPhoneNumber(contact.getContactPhone());

        return new ContactDTO(
                contact.getId(),
                contact.getContactName(),
                contact.getContactPhone(),
                isRegistered
        );
    }
}