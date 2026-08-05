package com.wipro.notificationservice.service;

import com.wipro.notificationservice.dto.NotificationRequest;
import com.wipro.notificationservice.util.CalendarInviteUtil;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationConfirmation(NotificationRequest req) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(req.getToEmail());
            message.setSubject("Registration Confirmed: " + req.getEventName());
            message.setText("Dear " + req.getUserName() + ",\n\n"
                    + "You have successfully registered for: " + req.getEventName() + "\n"
                    + "Date: " + req.getEventDate() + "\n"
                    + "Time: " + req.getEventTime() + "\n\n"
                    + "Thank you,\nThe EVENTRA Team");
            mailSender.send(message);
            log.info("Registration confirmation sent to {}", req.getToEmail());
        } catch (Exception ex) {
            log.error("Failed to send registration confirmation to {}", req.getToEmail(), ex);
        }
    }

    public void sendCalendarInvite(NotificationRequest req) {
        // Only send calendar invites for virtual/hybrid events
        if (req.getEventType() == null ||
                (!req.getEventType().equalsIgnoreCase("VIRTUAL") && !req.getEventType().equalsIgnoreCase("HYBRID"))) {
            log.info("Skipping calendar invite for non-virtual/non-hybrid event: {}", req.getEventName());
            return;
        }

        try {
            String icsContent = CalendarInviteUtil.generateICS(
                    req.getEventName(), req.getEventDescription(),
                    req.getEventDate(), req.getEventTime(), req.getAddress());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(req.getToEmail());
            helper.setSubject("Event Reminder: " + req.getEventName());
            helper.setText("You are registered for " + req.getEventName() + ". Please find the calendar invite attached.");
            helper.addAttachment("invite.ics", new ByteArrayResource(icsContent.getBytes()), "text/calendar");
            mailSender.send(message);
            log.info("Calendar invite sent to {}", req.getToEmail());
        } catch (Exception ex) {
            log.error("Failed to send calendar invite to {}", req.getToEmail(), ex);
        }
    }

    public void sendTicketConfirmation(NotificationRequest req) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(req.getToEmail());
            message.setSubject("Your Ticket for: " + req.getEventName());
            message.setText("Dear " + req.getUserName() + ",\n\n"
                    + "Your ticket has been confirmed for: " + req.getEventName() + "\n"
                    + "Date: " + req.getEventDate() + "\n"
                    + "Registration ID: " + req.getRegistrationId() + "\n\n"
                    + "Thank you,\nThe EVENTRA Team");
            mailSender.send(message);
            log.info("Ticket confirmation sent to {}", req.getToEmail());
        } catch (Exception ex) {
            log.error("Failed to send ticket confirmation to {}", req.getToEmail(), ex);
        }
    }
}
