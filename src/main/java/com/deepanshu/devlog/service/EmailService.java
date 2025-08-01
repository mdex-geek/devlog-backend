package com.deepanshu.devlog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private  JavaMailSender mailSender;

    public boolean sendOtp(String to, String otp,int expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText("Your OTP for email verification is: " + otp + ". It is valid for " + expiryMinutes + " minutes.");
        mailSender.send(message);
        return true;
    }

    public void sendTaskAssignmentEmail(String to, String taskTitle, String assignedBy) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You have been assigned a new task");
        message.setText("Hello,\n\nYou have been assigned the task: \"" + taskTitle +
                "\" by " + assignedBy + ".\n\nPlease check your dashboard.\n\n- DevLog");

        mailSender.send(message);
    }
}
