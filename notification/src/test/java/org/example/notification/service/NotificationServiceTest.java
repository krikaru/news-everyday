package org.example.notification.service;

import org.example.notification.model.NotificationRequest;
import org.example.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Test
    void checkBuildMailMessage() {
        NotificationRequest notificationRequest = new NotificationRequest("toUser@test.ru", "subject", "message");
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationService notificationService = new NotificationService(mailSender, "from@mail.ru");
        SimpleMailMessage requireMailMessage = new SimpleMailMessage();
        requireMailMessage.setFrom("from@mail.ru");
        requireMailMessage.setTo(notificationRequest.getToUserEmail());
        requireMailMessage.setSubject(notificationRequest.getSubject());
        requireMailMessage.setText(notificationRequest.getMessage());
        ArgumentCaptor<SimpleMailMessage> capturedMessage = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(isA(SimpleMailMessage.class));

        notificationService.send(notificationRequest);

        verify(mailSender).send(capturedMessage.capture());
        assertThat(capturedMessage.getValue())
                .as("")
                .isEqualTo(requireMailMessage);
    }
}