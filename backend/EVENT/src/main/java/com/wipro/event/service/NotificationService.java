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
import com.wipro.event.entity.Users;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://DEMP/api/user/";

    public void sendCalendarInvite(Registrations registration) throws Exception {
        Event event = registration.getEvent();

        int userId = registration.getUserId();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("No request context available for forwarding JWT token");
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String authHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Users> response = restTemplate.exchange(USER_SERVICE_URL + userId, HttpMethod.GET, entity, Users.class);
        Users user = response.getBody();

        String icsContent = CalendarInviteUtil.generateICS(event, null);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject("Event Reminder: " + event.getEventName());
        helper.setText("This is a reminder for your upcoming event: " + event.getEventName() +
    ". Please find the calendar invite attached. You will receive a reminder 30 minutes before the event.");
        helper.addAttachment("invite.ics", new ByteArrayResource(icsContent.getBytes()), "text/calendar");

        mailSender.send(message);
    }
}