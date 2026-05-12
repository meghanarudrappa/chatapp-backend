//package com.chatapp.chatapp.service;
//
//import com.chatapp.chatapp.model.User;
//import com.chatapp.chatapp.repository.UserRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    /**
//     * 🛡️ Standardizes phone numbers to ensure DB lookups always match.
//     */
//    public String cleanNumber(String raw) {
//        if (raw == null) return null;
//        return raw.replaceAll("[^0-9+]", "");
//    }
//
//    /**
//     * Used by AuthController during login.
//     * If this returns a User with a displayName, the app skips Setup.
//     */
//    public User findByPhoneNumber(String phoneNumber) {
//        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
//        String cleaned = cleanNumber(phoneNumber);
//        return userRepository.findByPhoneNumber(cleaned).orElse(null);
//    }
//
//    /**
//     * Handles both initial registration and profile updates (Image & Name).
//     */
//    public User registerUser(User user) {
//        if (user == null || user.getPhoneNumber() == null) return null;
//
//        // Ensure formatting is consistent before saving
//        user.setPhoneNumber(cleanNumber(user.getPhoneNumber()));
//
//        // .save() handles the persistence of profileImage and displayName
//        return userRepository.save(user);
//    }
//
//    /**
//     * Dual Search: Checks if the input is a number or a name.
//     */
//    public List<User> searchUsers(String query) {
//        if (query == null || query.isEmpty()) return new ArrayList<>();
//
//        String cleanedQuery = query.trim();
//
//        // If query is numeric (optionally starting with +), search by Phone
//        if (cleanedQuery.matches("\\+?\\d+")) {
//            return userRepository.findByPhoneNumberContaining(cleanedQuery);
//        }
//
//        // Otherwise, search by Name
//        return userRepository.findByDisplayNameContainingIgnoreCase(cleanedQuery);
//    }
//
//    public List<User> getContactsForUser(String phoneNumber) {
//        String cleaned = cleanNumber(phoneNumber);
//        return userRepository.findByPhoneNumber(cleaned)
//                .map(user -> {
//                    List<String> contactNumbers = user.getContacts();
//                    if (contactNumbers == null || contactNumbers.isEmpty()) {
//                        return new ArrayList<User>();
//                    }
//                    return userRepository.findByPhoneNumberIn(contactNumbers);
//                })
//                .orElse(new ArrayList<>());
//    }
//
//    public List<User> syncContacts(List<String> contactNumbers) {
//        if (contactNumbers == null) return new ArrayList<>();
//
//        List<String> cleanedNumbers = contactNumbers.stream()
//                .map(this::cleanNumber)
//                .filter(num -> num != null && !num.isEmpty())
//                .collect(Collectors.toList());
//
//        return userRepository.findByPhoneNumberIn(cleanedNumbers);
//    }
//
//    public boolean existsByPhoneNumber(String phoneNumber) {
//        if (phoneNumber == null) return false;
//        return userRepository.existsByPhoneNumber(cleanNumber(phoneNumber));
//    }
//}


package com.chatapp.chatapp.service;

import com.chatapp.chatapp.model.User;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 🛡️ Standardizes phone numbers to ensure DB lookups always match.
     */
    public String cleanNumber(String raw) {
        if (raw == null) return null;
        // Removes everything except digits and the plus sign
        return raw.replaceAll("[^0-9+]", "");
    }

    /**
     * Used by AuthController during login.
     * If this returns a User with a displayName, the app skips Setup.
     */
    public User findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        String cleaned = cleanNumber(phoneNumber);
        return userRepository.findByPhoneNumber(cleaned).orElse(null);
    }

    /**
     * 🚀 THE FIX: Handles registration/updates WITHOUT overwriting existing data.
     * It looks for the user in the DB first to prevent duplicates or blanking out names.
     */
    public User registerUser(User user) {
        if (user == null || user.getPhoneNumber() == null) return null;

        String cleanedPhone = cleanNumber(user.getPhoneNumber());

        // Check if user already exists in MongoDB
        User existingUser = userRepository.findByPhoneNumber(cleanedPhone).orElse(null);

        if (existingUser != null) {
            // 🛡️ UPDATE LOGIC: Only update fields if the new data isn't null/empty
            if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                existingUser.setDisplayName(user.getDisplayName().trim());
            }

            if (user.getProfileImage() != null) {
                existingUser.setProfileImage(user.getProfileImage());
            }

            // Save the UPDATED existing user (keeps the same MongoDB _id)
            return userRepository.save(existingUser);
        } else {
            // 🛡️ NEW USER LOGIC: Set phone and save as new document
            user.setPhoneNumber(cleanedPhone);
            // Ensure display name is at least an empty string, not null
            if (user.getDisplayName() == null) user.setDisplayName("");
            return userRepository.save(user);
        }
    }

    /**
     * Dual Search: Checks if the input is a number or a name.
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.isEmpty()) return new ArrayList<>();

        String cleanedQuery = query.trim();

        // If query is numeric (optionally starting with +), search by Phone
        if (cleanedQuery.matches("\\+?\\d+")) {
            return userRepository.findByPhoneNumberContaining(cleanedQuery);
        }

        // Otherwise, search by Name
        return userRepository.findByDisplayNameContainingIgnoreCase(cleanedQuery);
    }

    public List<User> getContactsForUser(String phoneNumber) {
        String cleaned = cleanNumber(phoneNumber);
        return userRepository.findByPhoneNumber(cleaned)
                .map(user -> {
                    List<String> contactNumbers = user.getContacts();
                    if (contactNumbers == null || contactNumbers.isEmpty()) {
                        return new ArrayList<User>();
                    }
                    return userRepository.findByPhoneNumberIn(contactNumbers);
                })
                .orElse(new ArrayList<>());
    }

    public List<User> syncContacts(List<String> contactNumbers) {
        if (contactNumbers == null) return new ArrayList<>();

        List<String> cleanedNumbers = contactNumbers.stream()
                .map(this::cleanNumber)
                .filter(num -> num != null && !num.isEmpty())
                .collect(Collectors.toList());

        return userRepository.findByPhoneNumberIn(cleanedNumbers);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        return userRepository.existsByPhoneNumber(cleanNumber(phoneNumber));
    }
}