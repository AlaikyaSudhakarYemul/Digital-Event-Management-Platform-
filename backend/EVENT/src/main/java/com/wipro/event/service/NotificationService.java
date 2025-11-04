package com.wipro.event.service;

import com.wipro.event.entity.Registrations;
import com.wipro.event.util.CalendarInviteUtil;
import com.wipro.event.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;
import com.wipro.event.entity.Users; // DTO/POJO for user response

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://DEMP/api/user/";

    public void sendCalendarInvite(Registrations registration) throws Exception {
        Event event = registration.getEvent();

        // Fetch user from user microservice
        int userId = registration.getUserId();
        Users user = restTemplate.getForObject(USER_SERVICE_URL + userId, Users.class);

        String icsContent = CalendarInviteUtil.generateICS(event, null);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject("Event Reminder: " + event.getEventName());
        helper.setText("You are registered for " + event.getEventName() + ". Please find the calendar invite attached.");
        helper.addAttachment("invite.ics", new ByteArrayResource(icsContent.getBytes()), "text/calendar");

        mailSender.send(message);
    }
}
