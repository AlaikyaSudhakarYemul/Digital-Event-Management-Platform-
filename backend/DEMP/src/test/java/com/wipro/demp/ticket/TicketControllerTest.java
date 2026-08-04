package com.wipro.demp.ticket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wipro.demp.auth.TestConfig;
import com.wipro.demp.controller.TicketController;
import com.wipro.demp.entity.Ticket;
import com.wipro.demp.entity.TicketType;
import com.wipro.demp.service.TicketService;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    private Ticket sampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setTicketId(1);
        ticket.setTicketType(TicketType.ECONOMY_CLASS);
        ticket.setPrice(new BigDecimal("499.00"));
        ticket.setEventId(10);
        ticket.setUserId(5);
        ticket.setRegistrationId(100);
        return ticket;
    }

    @Test
    void createTicketSuccess() throws Exception {
        when(ticketService.createTicket(any(Ticket.class))).thenReturn(sampleTicket());

        String payload = """
                {
                  "ticketType": "ECONOMY_CLASS",
                  "price": 499.00,
                  "eventId": 10,
                  "userId": 5,
                  "registrationId": 100
                }
                """;

        mockMvc.perform(post("/api/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ticketId").value(1));
    }

    @Test
    void createMultipleTicketsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/tickets/create-multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid ticket request payload."));
    }

    @Test
    void createMultipleTicketsSuccess() throws Exception {
        when(ticketService.createMultipleTickets(any(Ticket.class), eq(2)))
            .thenReturn(List.of(sampleTicket(), sampleTicket()));

        String payload = """
                {
                  "ticketType": "ECONOMY_CLASS",
                  "price": 499.00,
                  "eventId": 10,
                  "userId": 5,
                  "registrationId": 100,
                  "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/tickets/create-multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTicketByIdNegativeId() throws Exception {
        mockMvc.perform(get("/api/tickets/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("ID must be apositive integer"));
    }

    @Test
    void getTicketByIdSuccess() throws Exception {
        when(ticketService.getTicketById(1)).thenReturn(sampleTicket());

        mockMvc.perform(get("/api/tickets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventId").value(10));
    }

    @Test
    void updateTicketInvalidId() throws Exception {
        when(ticketService.updateTicket(eq(-1), any(Ticket.class))).thenReturn(sampleTicket());

        String payload = """
                {
                  "ticketType": "ECONOMY_CLASS",
                  "price": 499.00,
                  "eventId": 10,
                  "userId": 5,
                  "registrationId": 100
                }
                """;

        mockMvc.perform(put("/api/tickets/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid ticket data or ticket not found."));
    }

    @Test
    void deleteTicketInvalidId() throws Exception {
        mockMvc.perform(delete("/api/tickets/-5"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("ID must be a positive integer"));
    }

    @Test
    void deleteTicketSuccess() throws Exception {
        doNothing().when(ticketService).deleteTicket(1);

        mockMvc.perform(delete("/api/tickets/1"))
            .andExpect(status().isNoContent());
    }
}
