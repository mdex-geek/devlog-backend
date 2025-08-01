package com.deepanshu.devlog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.EmailVerificationToken;
import com.deepanshu.devlog.Entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByOtp(String otp);
;

    void deleteByUser(User user);

}
