package com.deepanshu.devlog.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    
    @Email(message = "Email should be valid")
    private String email;
    
    // Device information for multiple device login
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String userAgent;
}
