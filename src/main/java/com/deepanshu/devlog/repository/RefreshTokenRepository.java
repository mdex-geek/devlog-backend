package com.deepanshu.devlog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.RefreshToken;
import com.deepanshu.devlog.Entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    // New methods for multiple device support
    List<RefreshToken> findByUser(User user);
    
    Optional<RefreshToken> findByUserAndDeviceId(User user, String deviceId);
    
    void deleteByUserAndDeviceId(User user, String deviceId);
    
    void deleteByToken(String token);
}
