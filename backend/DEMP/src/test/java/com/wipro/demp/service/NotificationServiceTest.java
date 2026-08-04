package com.wipro.demp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventType;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.UserRepository;
import com.wipro.demp.util.CalendarInviteUtil;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Registrations buildRegistration(EventType type) {
        Event event = new Event();
        event.setEventId(100);
        event.setEventName("Summit");
        event.setDescription("Technology conference description");
        event.setDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(10, 0));
        event.setEventType(type);

        Users user = new Users();
        user.setUserId(1);

        Registrations registration = new Registrations();
        registration.setRegistrationId(10);
        registration.setEvent(event);
        registration.setUser(user);
        return registration;
    }

    @Test
    void sendCalendarInviteSkipsForInPersonEvents() throws Exception {
        Registrations registration = buildRegistration(EventType.IN_PERSON);

        notificationService.sendCalendarInvite(registration);

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(userRepository, never()).getById(any());
    }

    @Test
    void sendCalendarInviteSendsForVirtualEvent() throws Exception {
        Registrations registration = buildRegistration(EventType.VIRTUAL);

        Users user = new Users();
        user.setUserId(1);
        user.setEmail("user@test.com");
        when(userRepository.getById(1)).thenReturn(user);

        MimeMessage mimeMessage = new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        try (MockedStatic<CalendarInviteUtil> mocked = org.mockito.Mockito.mockStatic(CalendarInviteUtil.class)) {
            mocked.when(() -> CalendarInviteUtil.generateICS(any(Event.class), any())).thenReturn("BEGIN:VCALENDAR\nEND:VCALENDAR");

            notificationService.sendCalendarInvite(registration);
        }

        verify(mailSender).send(any(MimeMessage.class));
    }
}
