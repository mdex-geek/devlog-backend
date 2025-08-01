package com.deepanshu.devlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.Notification;
import com.deepanshu.devlog.Entity.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndReadFalse(User user);
}