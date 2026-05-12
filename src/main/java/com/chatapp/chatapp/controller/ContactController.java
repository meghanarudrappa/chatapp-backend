package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.dto.ContactDTO;
import com.chatapp.chatapp.model.Contact;
import com.chatapp.chatapp.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts") // This matches your frontend's API.post('/contacts/add')
public class ContactController {

    @Autowired
    private ContactService contactService;

    // This method will now "use" the saveContact method in your service
    @PostMapping("/add")
    public ResponseEntity<ContactDTO> addContact(@RequestBody Contact contact) {
        ContactDTO savedContact = contactService.saveContact(contact);
        return ResponseEntity.ok(savedContact);
    }

    // Endpoint to fetch all contacts for a specific user
    @GetMapping("/{ownerPhone}")
    public ResponseEntity<List<ContactDTO>> getContacts(@PathVariable String ownerPhone) {
        return ResponseEntity.ok(contactService.getAllMyContacts(ownerPhone));
    }
}