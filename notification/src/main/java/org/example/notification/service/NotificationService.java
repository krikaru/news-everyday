package org.example.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.notification.model.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Autowired
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;

    public void send(NotificationRequest notificationRequest) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(username);
        mailMessage.setTo(notificationRequest.getToUserEmail());
        mailMessage.setSubject(notificationRequest.getSubject());
        mailMessage.setText(notificationRequest.getMessage());

        mailSender.send(mailMessage);
    }
}
