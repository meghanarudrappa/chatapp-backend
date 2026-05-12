package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.model.User;
import com.chatapp.chatapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/sync")
    public ResponseEntity<List<User>> sync(@RequestBody List<String> contactNumbers) {
        return ResponseEntity.ok(userService.syncContacts(contactNumbers));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam String query) {
        if (query.length() < 3) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @GetMapping("/contacts/{phoneNumber}")
    public ResponseEntity<List<User>> getContactsByPhone(@PathVariable String phoneNumber) {
        List<User> contacts = userService.getContactsForUser(phoneNumber);
        return ResponseEntity.ok(contacts);
    }


    @GetMapping("/check/{phoneNumber}")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String phoneNumber) {
        boolean exists = userService.existsByPhoneNumber(phoneNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}