package com.deepanshu.devlog.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.deepanshu.devlog.Entity.RefreshToken;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.repository.RefreshTokenRepository;
import com.deepanshu.devlog.utils.TokenGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    //refresh token valid for 30 days
    public static final long REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60 * 1000;

    // Generates & stores a new token for a specific device
    public RefreshToken createRefreshToken(User user, String deviceId, String deviceName, String deviceType, String userAgent) {
        // Check if device already has a token, if so, delete the old one
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
        }

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(TokenGenerator.generateSecureToken());
        token.setExpiryDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
        token.setDeviceId(deviceId);
        token.setDeviceName(deviceName);
        token.setDeviceType(deviceType);
        token.setUserAgent(userAgent);
        token.setLastUsedAt(new Date());

        return refreshTokenRepository.save(token);
    }

    // Backward compatibility method
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, "default-device", "Default Device", "unknown", "unknown");
    }

    // Finds it by value
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Checks if token is expired
    public boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().before(new Date());
    }

    // Delete all tokens for a user (logout from all devices)
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // Delete token for specific device
    public void deleteByUserAndDeviceId(User user, String deviceId) {
        refreshTokenRepository.deleteByUserAndDeviceId(user, deviceId);
    }

    // Get all active sessions for a user
    public List<RefreshToken> getUserSessions(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    // Update last used timestamp
    public void updateLastUsed(RefreshToken token) {
        token.setLastUsedAt(new Date());
        refreshTokenRepository.save(token);
    }

    // Delete expired tokens
    public void deleteExpiredTokens() {
        List<RefreshToken> allTokens = refreshTokenRepository.findAll();
        for (RefreshToken token : allTokens) {
            if (isExpired(token)) {
                refreshTokenRepository.delete(token);
            }
        }
    }
}
