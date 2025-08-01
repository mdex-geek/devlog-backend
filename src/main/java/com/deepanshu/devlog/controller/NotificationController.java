package com.deepanshu.devlog.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepanshu.devlog.Entity.Notification;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.repository.NotificationRepository;
import com.deepanshu.devlog.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
@Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Notification> getUnreadNotifications(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        return notificationRepository.findByRecipientAndReadFalse(user);
    }

    @PostMapping("/mark-read/{id}")
    public void markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
