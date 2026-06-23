package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.response.NotificationResponse;
import com.amritesh.moviebooking.entity.Notification;
import com.amritesh.moviebooking.entity.User;
import com.amritesh.moviebooking.entity.enums.NotificationStatus;
import com.amritesh.moviebooking.entity.enums.NotificationType;
import com.amritesh.moviebooking.mapper.NotificationMapper;
import com.amritesh.moviebooking.repository.NotificationRepository;
import com.amritesh.moviebooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Delivers notifications asynchronously so the booking flow never blocks on them.
 * Delivery is simulated by persisting a {@link Notification} and logging it.
 * Methods accept primitive data (not entities) so they are safe to run on a
 * separate thread without touching lazy associations.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    @Async("notificationExecutor")
    @Transactional
    public void send(Long userId, NotificationType type, String message) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Skipping notification; user {} not found", userId);
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        // Simulated delivery.
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("[NOTIFY:{}] to user {} -> {}", type, userId, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}
