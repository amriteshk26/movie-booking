package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.response.NotificationResponse;
import com.amritesh.moviebooking.security.SecurityUtils;
import com.amritesh.moviebooking.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lets an authenticated user view their notification history.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public List<NotificationResponse> myNotifications() {
        return notificationService.findForUser(securityUtils.getCurrentUserId());
    }
}
