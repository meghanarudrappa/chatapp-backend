package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.dto.AuthRequest;
import com.chatapp.chatapp.model.User;
import com.chatapp.chatapp.security.JwtProvider;
import com.chatapp.chatapp.service.OtpService;
import com.chatapp.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserService userService;

    private final OtpService otpService;

    public AuthController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody AuthRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }
        String cleanedPhone = userService.cleanNumber(request.getPhoneNumber());
        otpService.generateAndSaveOtp(cleanedPhone);
        return ResponseEntity.ok("OTP Sent Successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody AuthRequest request) {
        String cleanedPhone = userService.cleanNumber(request.getPhoneNumber());
        boolean isValid = otpService.verifyOtp(cleanedPhone, request.getOtpCode());

        if (isValid) {
            String token = jwtProvider.generateToken(cleanedPhone);
            User user = userService.findByPhoneNumber(cleanedPhone);

            // 🚀 Logic: Create user if first time login
            if (user == null) {
                user = new User();
                user.setPhoneNumber(cleanedPhone);
                user.setDisplayName("");
                user = userService.registerUser(user);
            }

            // 🚀 THE FIX FOR FRONTEND SKIP LOGIC:
            // We wrap everything in a way that matches your React Native destructuring:
            // const { token, user } = response.data;
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user", user); // This contains displayName, phoneNumber, profileImage

            return ResponseEntity.ok(responseData);
        }
        return ResponseEntity.status(401).body("Invalid or Expired OTP");
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updateData) {
        String cleanedPhone = userService.cleanNumber(updateData.getPhoneNumber());
        User user = userService.findByPhoneNumber(cleanedPhone);

        if (user != null) {
            boolean isChanged = false;

            if (updateData.getDisplayName() != null && !updateData.getDisplayName().trim().isEmpty()) {
                user.setDisplayName(updateData.getDisplayName().trim());
                isChanged = true;
            }

            if (updateData.getProfileImage() != null) {
                user.setProfileImage(updateData.getProfileImage());
                isChanged = true;
            }

            if (isChanged) {
                userService.registerUser(user);
            }

            // Return the updated user object wrapped for consistency
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body("User not found");
    }
}