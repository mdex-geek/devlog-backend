package com.deepanshu.devlog.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepanshu.devlog.Entity.EmailVerificationToken;
import com.deepanshu.devlog.Entity.RefreshToken;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.dto.AuthRequest;
import com.deepanshu.devlog.dto.AuthResponse;
import com.deepanshu.devlog.dto.DeviceSession;
import com.deepanshu.devlog.dto.RefreshRequest;
import com.deepanshu.devlog.repository.EmailVerificationTokenRepository;
import com.deepanshu.devlog.repository.UserRepository;
import com.deepanshu.devlog.service.EmailService;
import com.deepanshu.devlog.service.JwtService;
import com.deepanshu.devlog.service.RefreshTokenService;
import com.deepanshu.devlog.utils.AccountStatus;
import com.deepanshu.devlog.utils.DeviceUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final EmailService emailService;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequest request) {
        // create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(AccountStatus.UNVERIFIED); // Set initial status to UNVERIFIED
        user.setEmail(request.getEmail());

        // generate otp
        String otp = String.format("%06d", new Random().nextInt(999999));

        // send otp with expiry
        EmailVerificationToken token = new EmailVerificationToken();
        token.setOtp(otp);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // Set expiry to 10 minutes

        // send email
        boolean result = emailService.sendOtp(user.getEmail(), otp, 10);

        if (!result) {
            ResponseEntity.internalServerError().body("there is issue our side re-login again");
        }
        userRepository.save(user);
        emailVerificationTokenRepository.save(token);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        logger.info("Login attempt for username: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE || !user.isVerified()) {
            logger.warn("Login failed for user {}: Account not verified. Status: {}, Verified: {}", 
                request.getUsername(), user.getStatus(), user.isVerified());
            throw new RuntimeException("Account not verified. Please verify your email before logging in.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed for user {}: Invalid password", request.getUsername());
            throw new RuntimeException("Invalid credentials");
        }

        // Robust device info handling for Postman and other clients
        String userAgentHeader = httpRequest.getHeader("User-Agent");
        String userAgent = (request.getUserAgent() != null && !request.getUserAgent().isEmpty())
                ? request.getUserAgent() : (userAgentHeader != null ? userAgentHeader : "Unknown");

        String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isEmpty())
                ? request.getDeviceId() : java.util.UUID.randomUUID().toString();

        String deviceName;
        if (request.getDeviceName() != null && !request.getDeviceName().isEmpty()) {
            deviceName = request.getDeviceName();
        } else if (userAgentHeader != null && userAgentHeader.toLowerCase().contains("Postman")) {
            deviceName = "Postman";
        } else if (userAgentHeader != null) {
            deviceName = DeviceUtils.generateDeviceName(userAgentHeader);
        } else {
            deviceName = "Unknown Device";
        }

        String deviceType = (request.getDeviceType() != null && !request.getDeviceType().isEmpty())
                ? request.getDeviceType() : (userAgentHeader != null ? DeviceUtils.detectDeviceType(userAgentHeader) : "Unknown");

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, deviceId, deviceName, deviceType, userAgent);

        logger.info("Login successful for user: {} (deviceId: {}, deviceName: {}, deviceType: {})", request.getUsername(), deviceId, deviceName, deviceType);
        return new AuthResponse(jwtService.generateToken(user.getUsername()), refreshToken.getToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestHeader RefreshRequest request) {
        String reqToken = request.getRefreshToken();

        RefreshToken token = refreshTokenService.findByToken(reqToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshTokenService.isExpired(token)) {
            refreshTokenService.deleteByUserAndDeviceId(token.getUser(), token.getDeviceId());
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        // Update last used timestamp
        refreshTokenService.updateLastUsed(token);

        String newAccessToken = jwtService.generateToken(token.getUser().getUsername());

        return new AuthResponse(newAccessToken, reqToken); // reuse same refreshToken or rotate if needed
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String otp = request.get("otp");

        EmailVerificationToken token = emailVerificationTokenRepository.findByOtp(otp)
                .orElse(null);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid OTP");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.delete(token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("OTP expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(token);

        return ResponseEntity.ok(" Email verified successfully!");
    }

    // Get all active sessions for the current user
    @GetMapping("/sessions")
    public ResponseEntity<List<DeviceSession>> getUserSessions(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RefreshToken> sessions = refreshTokenService.getUserSessions(user);
        
        List<DeviceSession> sessionDtos = sessions.stream()
                .map(this::convertToDeviceSessionDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessionDtos);
    }

    // Logout from specific device
    @DeleteMapping("/sessions/{deviceId}")
    public ResponseEntity<String> logoutFromDevice(@PathVariable String deviceId, HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.deleteByUserAndDeviceId(user, deviceId);
        
        return ResponseEntity.ok("Logged out from device: " + deviceId);
    }

    // Logout from all devices
    @DeleteMapping("/sessions")
    public ResponseEntity<String> logoutFromAllDevices(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.deleteByUser(user);
        
        return ResponseEntity.ok("Logged out from all devices");
    }

    // Helper method to extract token from Authorization header
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }



    // Helper method to convert RefreshToken to DeviceSessionDto
    private DeviceSession convertToDeviceSessionDto(RefreshToken token) {
        DeviceSession dto = new DeviceSession();
        dto.setDeviceId(token.getDeviceId());
        dto.setDeviceName(token.getDeviceName());
        dto.setDeviceType(token.getDeviceType());
        dto.setUserAgent(token.getUserAgent());
        dto.setLastUsedAt(token.getLastUsedAt());
        dto.setExpiryDate(token.getExpiryDate());
        dto.setCurrentDevice(false); // This would need to be set based on current request
        return dto;
    }
}
