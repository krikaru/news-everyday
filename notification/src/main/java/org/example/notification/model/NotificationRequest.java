package org.example.notification.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NotificationRequest {
    private String toUserEmail;
    private String subject;
    private String message;
}
