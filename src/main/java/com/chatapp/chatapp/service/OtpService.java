package com.chatapp.chatapp.service;

import com.chatapp.chatapp.model.OtpRequest;
import com.chatapp.chatapp.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public void generateAndSaveOtp(String phone) {
        Optional<OtpRequest> existing = otpRepository.findByPhoneNumber(phone);

        // Generates a 6-digit code
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        OtpRequest otpRequest;

        if (existing.isPresent()) {
            otpRequest = existing.get();
            otpRequest.setOtpCode(code);
            otpRequest.setCreatedAt(LocalDateTime.now());
        } else {
            otpRequest = new OtpRequest(phone, code);
        }

        otpRepository.save(otpRequest);
        System.out.println("DEBUG: OTP for " + phone + " is " + code);
    }

    public boolean verifyOtp(String phone, String otpCode) {
        // 1. Look for the record in MongoDB
        Optional<OtpRequest> otpData = otpRepository.findByPhoneNumber(phone);

        if (otpData.isPresent()) {
            OtpRequest record = otpData.get();

            // 2. Check if OTP matches
            boolean isMatch = record.getOtpCode().equals(otpCode);

            // 3. Optional: Check if expired (e.g., older than 5 minutes)
            boolean isNotExpired = record.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5));

            if (isMatch && isNotExpired) {
                // 4. Delete the OTP after successful use so it can't be reused
                otpRepository.delete(record);
                return true;
            }
        }

        // If phone not found, code doesn't match, or expired
        return false;
    }
}