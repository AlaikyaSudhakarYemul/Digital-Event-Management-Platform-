
package com.wipro.demp.event;

import com.wipro.demp.auth.*;
import com.wipro.demp.controller.EventController;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.service.EventService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private EventService eventService;

    private Event buildSampleEvent() {
        Event event = new Event();
        event.setEventId(1);
        event.setEventName("AI Summit");
        event.setDescription("Annual AI Conference");
        event.setDate(LocalDate.of(2025, 12, 15));
        event.setActiveStatus(EventStatus.ACTIVE);
        event.setCreatedOn(LocalDate.of(2025, 6, 14));
        event.setCreationTime(LocalDateTime.of(2025, 6, 14, 10, 0));
        return event;
    }

    @Test
    void createEventSuccess() throws Exception {
        Event event = buildSampleEvent();
        Mockito.when(eventService.createEvent(any())).thenReturn(event);

        String jsonPayload = """
                {
                    "eventName": "AI Summit",
                    "description": "Annual AI Conference",
                    "date": "2025-12-15",
                    "activeStatus": "ACTIVE",
                    "createdOn": "2025-06-14"
                }
                """;

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventName").value("AI Summit"));
    }

    @Test
    void getEventByIdValidId() throws Exception {
        Mockito.when(eventService.getEventById(1)).thenReturn(buildSampleEvent());

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("AI Summit"));
    }

    @Test
    void getEventByIdInvalidId() throws Exception {
        mockMvc.perform(get("/api/events/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"))
                .andExpect(jsonPath("$.details").value("ID must be a positive integer"));
    }

    @Test
    void getAllEvents() throws Exception {
        Mockito.when(eventService.getAllEvents()).thenReturn(List.of(buildSampleEvent()));

        mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateEventSuccess() throws Exception {
        Event updated = buildSampleEvent();
        updated.setEventName("AI Expo");
        Mockito.when(eventService.updateEvent(eq(1), any())).thenReturn(updated);

        String jsonPayload = """
                {
                    "eventId": 1,
                    "eventName": "AI Expo",
                    "description": "Annual AI Conference",
                    "date": "2025-12-15",
                    "activeStatus": "ACTIVE",
                    "createdOn": "2025-06-14",
                    "creationTime": "2025-06-14T10:00:00"
                }
                """;

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("AI Expo"));
    }

    @Test
    void updateEventInvalidInput() throws Exception {
        String invalidJson = """
                {
                    "eventName": null,
                    "description": null
                }
                """;

        mockMvc.perform(put("/api/events/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request body."));
    }

    @Test
    void deleteEventValidId() throws Exception {
        mockMvc.perform(delete("/api/events/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event deleted successfully"));
    }

    @Test
    void deleteEventInvalidId() throws Exception {
        mockMvc.perform(delete("/api/events/-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter"))
                .andExpect(jsonPath("$.details").value("ID must be a positive integer"));
    }

    @Test
    void searchEventsValidName() throws Exception {
        Mockito.when(eventService.findByEventName("AI Summit"))
                .thenReturn(Collections.singletonList(buildSampleEvent()));

        mockMvc.perform(get("/api/events/search")
                        .param("eventName", "AI Summit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName").value("AI Summit"));
    }

    @Test
    void searchEventsEmptyParam() throws Exception {
        mockMvc.perform(get("/api/events/search")
                        .param("eventName", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid event name."));
    }
}