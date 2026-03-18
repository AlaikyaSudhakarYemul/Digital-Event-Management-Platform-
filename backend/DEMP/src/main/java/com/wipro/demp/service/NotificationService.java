package com.wipro.demp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;

import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventType;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.UserRepository;
import com.wipro.demp.util.CalendarInviteUtil;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private UserRepository userRepository;

    // private final String USER_SERVICE_URL = "http://DEMP/api/user/";

    public void sendCalendarInvite(Registrations registration) throws Exception {
        Event event = registration.getEvent();

        // Send invites only for virtual and hybrid events.
        if (event == null ||
            !(EventType.VIRTUAL.equals(event.getEventType()) || EventType.HYBRID.equals(event.getEventType()))) {
            log.info("Skipping calendar invite for non-virtual/non-hybrid event registration {}",
                    registration != null ? registration.getRegistrationId() : null);
            return;
        }

        int userId = registration.getUser().getUserId();
        
        Users user = userRepository.getById(userId);

        String icsContent = CalendarInviteUtil.generateICS(event, event.getAddress());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject("Event Reminder: " + event.getEventName());
        helper.setText("You are registered for " + event.getEventName() + ". Please find the calendar invite attached.");
        helper.addAttachment("invite.ics", new ByteArrayResource(icsContent.getBytes()), "text/calendar");

        mailSender.send(message);
    }
}