package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.NotificationStatus;
import com.amritesh.moviebooking.entity.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {
}
