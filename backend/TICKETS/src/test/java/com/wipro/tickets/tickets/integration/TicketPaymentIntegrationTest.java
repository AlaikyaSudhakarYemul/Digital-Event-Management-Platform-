package com.wipro.tickets.tickets.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wipro.tickets.tickets.entity.Payment;
import com.wipro.tickets.tickets.entity.PaymentStatus;
import com.wipro.tickets.tickets.entity.Ticket;
import com.wipro.tickets.tickets.entity.TicketStatus;
import com.wipro.tickets.tickets.repositoty.PaymentRepository;
import com.wipro.tickets.tickets.repositoty.TicketRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setupExternalValidationMocks() {
        ResponseEntity<Object> okResponse = ResponseEntity.ok(Map.of("ok", true));

        org.mockito.Mockito.when(restTemplate.exchange(
                        contains("/api/user/"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(Object.class)))
                .thenReturn(okResponse);

        org.mockito.Mockito.when(restTemplate.exchange(
                        contains("/api/events/"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(Object.class)))
                .thenReturn(okResponse);
    }

    @Test
    @WithMockUser(roles = "USER")
    void bookingAndSuccessfulPaymentShouldConfirmTicket() throws Exception {
        String bookingPayload = """
                {
                  "eventId": 2001,
                  "userId": 1001,
                  "quantity": 2,
                  "totalAmount": 499.98
                }
                """;

        MvcResult bookingResult = mockMvc.perform(post("/api/tickets/book")
                        .contentType("application/json")
                        .content(bookingPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andReturn();

        JsonNode bookingJson = objectMapper.readTree(bookingResult.getResponse().getContentAsString());
        long ticketId = bookingJson.get("ticketId").asLong();

        String paymentPayload = String.format("""
                {
                  "ticket": {"ticketId": %d},
                  "amount": 499.98,
                  "paymentStatus": "SUCCESS",
                  "transactionId": "txn-1001"
                }
                """, ticketId);

        mockMvc.perform(post("/api/payments")
                        .contentType("application/json")
                        .content(paymentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));

        Ticket persistedTicket = ticketRepository.findById(ticketId).orElseThrow();
        assertThat(persistedTicket.getStatus()).isEqualTo(TicketStatus.CONFIRMED);
    }

    @Test
    @WithMockUser(roles = "USER")
    void refundShouldCancelTicket() throws Exception {
        Ticket ticket = Ticket.builder()
                .eventId(3001)
                .userId(2001)
                .quantity(1)
                .totalAmount(new BigDecimal("250.00"))
                .status(TicketStatus.CONFIRMED)
                .isDeleted(false)
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);

        Payment payment = Payment.builder()
                .ticket(savedTicket)
                .amount(new BigDecimal("250.00"))
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn-2001")
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        mockMvc.perform(post("/api/payments/{paymentId}/refund", savedPayment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));

        Ticket updatedTicket = ticketRepository.findById(savedTicket.getTicketId()).orElseThrow();
        assertThat(updatedTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }
}
