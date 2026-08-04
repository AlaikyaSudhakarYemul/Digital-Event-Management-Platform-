package com.wipro.demp.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.Payment;
import com.wipro.demp.entity.PaymentStatus;
import com.wipro.demp.entity.Ticket;
import com.wipro.demp.entity.TicketType;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.PaymentsRepository;
import com.wipro.demp.repository.TicketRepository;
import com.wipro.demp.repository.UserRepository;
import com.wipro.demp.service.TicketServiceImpl;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PaymentsRepository paymentsRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ticketService, "backendBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(ticketService, "frontendBaseUrl", "http://localhost:3000");
    }

    private Ticket ticketTemplate() {
        Ticket t = new Ticket();
        t.setTicketType(TicketType.FIRST_CLASS);
        t.setPrice(new BigDecimal("999.00"));
        t.setEventId(1);
        t.setUserId(2);
        t.setRegistrationId(3);
        return t;
    }

    @Test
    void createMultipleTicketsRejectsQuantityOverLimit() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createMultipleTickets(ticketTemplate(), 6));

        assertEquals("You can select at most 5 tickets at a time.", ex.getMessage());
    }

    @Test
    void createMultipleTicketsRejectsWhenExistingMaxReached() {
        when(ticketRepository.countByUserIdAndEventIdAndIsDeletedFalse(2, 1)).thenReturn(5L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ticketService.createMultipleTickets(ticketTemplate(), 1));

        assertEquals("Maximum limit reached. You can buy only 5 tickets for this event.", ex.getMessage());
    }

    @Test
    void createMultipleTicketsSuccessAndEmailBestEffort() {
        Ticket template = ticketTemplate();

        when(ticketRepository.countByUserIdAndEventIdAndIsDeletedFalse(2, 1)).thenReturn(0L);
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<Ticket> saved = (List<Ticket>) invocation.getArgument(0);
            int id = 100;
            for (Ticket t : saved) {
                t.setTicketId(id++);
            }
            return saved;
        });

        Users user = new Users();
        user.setEmail("user@test.com");
        user.setUserName("Test User");
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        Event event = new Event();
        event.setEventName("Demo Event");
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        List<Ticket> tickets = ticketService.createMultipleTickets(template, 2);

        assertEquals(2, tickets.size());
        assertNotNull(tickets.get(0).getCreatedOn());
        verify(ticketRepository).saveAll(any());
        verify(mailSender, times(2)).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void getTicketByIdPopulatesPaymentStatusWhenPaymentExists() {
        Ticket ticket = ticketTemplate();
        ticket.setTicketId(44);
        when(ticketRepository.findById(44)).thenReturn(Optional.of(ticket));

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentsRepository.findTopByRegistrationIdOrderByIdDesc(3L)).thenReturn(Optional.of(payment));

        Ticket result = ticketService.getTicketById(44);

        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
    }

    @Test
    void getAllTicketsReturnsEmptyWithoutPaymentQueries() {
        when(ticketRepository.findAll()).thenReturn(List.of());

        List<Ticket> result = ticketService.getAllTickets();

        assertTrue(result.isEmpty());
        verify(paymentsRepository, never()).findTopByRegistrationIdOrderByIdDesc(any());
    }

    @Test
    void deleteTicketSoftDeletes() {
        Ticket ticket = ticketTemplate();
        ticket.setTicketId(10);
        when(ticketRepository.findById(10)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(10);

        assertTrue(ticket.isDeleted());
        verify(ticketRepository).save(ticket);
    }
}
