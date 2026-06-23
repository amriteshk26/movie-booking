package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.Booking;
import com.amritesh.moviebooking.entity.enums.BookingStatus;
import com.amritesh.moviebooking.entity.enums.NotificationType;
import com.amritesh.moviebooking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sends a one-time reminder for confirmed bookings whose show starts within the
 * reminder window. Delivery is asynchronous (via {@link NotificationService}) so
 * it never interferes with the booking flow. The {@code reminderSent} flag
 * guards against duplicates.
 */
@Service
public class ShowReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShowReminderScheduler.class);

    /** Remind for shows starting within this many minutes from now. */
    private static final long REMINDER_WINDOW_MINUTES = 120;

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public ShowReminderScheduler(BookingRepository bookingRepository,
                                 NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${booking.hold.sweep-interval-ms}")
    @Transactional
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(REMINDER_WINDOW_MINUTES);

        List<Booking> due = bookingRepository
                .findByStatusAndReminderSentFalseAndShowStartTimeBetween(
                        BookingStatus.CONFIRMED, now, windowEnd);
        if (due.isEmpty()) {
            return;
        }
        for (Booking booking : due) {
            String message = String.format(
                    "Reminder: '%s' starts at %s. Booking #%d.",
                    booking.getShow().getMovie().getTitle(),
                    booking.getShow().getStartTime(),
                    booking.getId());
            notificationService.send(booking.getUser().getId(), NotificationType.SHOW_REMINDER, message);
            booking.setReminderSent(true);
        }
        log.info("Queued {} show reminder(s)", due.size());
    }
}
