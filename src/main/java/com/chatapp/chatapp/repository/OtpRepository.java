package com.chatapp.chatapp.repository;


import com.chatapp.chatapp.model.OtpRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpRepository extends MongoRepository<OtpRequest, String> {
    // Spring generates this query: find where phoneNumber AND otpCode match
    Optional<OtpRequest> findByPhoneNumberAndOtpCode(String phoneNumber, String otpCode);

    // Optional: Delete all OTPs for a number after successful login
    void deleteByPhoneNumber(String phoneNumber);

    Optional<OtpRequest> findByPhoneNumber(String phone);
}
