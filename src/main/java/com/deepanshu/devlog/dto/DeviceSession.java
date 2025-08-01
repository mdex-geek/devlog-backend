package com.deepanshu.devlog.dto;

import java.util.Date;

import lombok.Data;

@Data
public class DeviceSession {
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String userAgent;
    private Date lastUsedAt;
    private Date expiryDate;
    private boolean currentDevice;
} 