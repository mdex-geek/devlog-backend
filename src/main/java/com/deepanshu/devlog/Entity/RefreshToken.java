package com.deepanshu.devlog.Entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Date expiryDate;

    // Device information for multiple device support
    @Column(nullable = true)
    private String deviceId;

    @Column(nullable = true)
    private String deviceName;

    @Column(nullable = true)
    private String deviceType; // "mobile", "desktop", "tablet", etc.

    @Column(nullable = true)
    private String userAgent;

    @Column
    private Date lastUsedAt;
}

