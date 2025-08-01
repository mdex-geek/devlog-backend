package com.deepanshu.devlog.utils;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtils {
    
    public static String generateDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        
        // Create a hash of user agent and IP for consistent device identification
        String deviceFingerprint = userAgent + "|" + remoteAddr;
        return java.util.UUID.nameUUIDFromBytes(deviceFingerprint.getBytes()).toString();
    }
    
    public static String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        } else if (userAgent.contains("windows") || userAgent.contains("mac") || userAgent.contains("linux")) {
            return "desktop";
        } else {
            return "unknown";
        }
    }
    
    public static String generateDeviceName(String userAgent) {
        if (userAgent == null) {
            return "Unknown Device";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("chrome")) {
            return "Chrome Browser";
        } else if (userAgent.contains("firefox")) {
            return "Firefox Browser";
        } else if (userAgent.contains("safari")) {
            return "Safari Browser";
        } else if (userAgent.contains("edge")) {
            return "Edge Browser";
        } else if (userAgent.contains("android")) {
            return "Android Device";
        } else if (userAgent.contains("iphone")) {
            return "iPhone";
        } else if (userAgent.contains("ipad")) {
            return "iPad";
        } else {
            return "Unknown Device";
        }
    }
} 