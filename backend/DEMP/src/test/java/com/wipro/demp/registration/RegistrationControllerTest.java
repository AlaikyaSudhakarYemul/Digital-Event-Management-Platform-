package com.wipro.demp.registration;


import com.wipro.demp.controller.RegistrationController;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.RegistrationStatus;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.service.RegistrationService;
import com.wipro.demp.config.JwtFilter;
import com.wipro.demp.entity.Users;
import com.wipro.demp.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegistrationController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private JwtUtil jwtUtil;

    private Registrations sampleReg;
    private Users sampleUser;
    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleUser = new Users();
        sampleUser.setUserId(1);
        sampleUser.setUserName("JohnDoe");

        sampleEvent = new Event();
        sampleEvent.setEventId(100);
        sampleEvent.setEventName("TechConf");

        sampleReg = new Registrations();
        sampleReg.setRegistrationId(1);
        sampleReg.setUser(sampleUser);
        sampleReg.setEvent(sampleEvent);
        sampleReg.setStatus(RegistrationStatus.REGISTERED);
        sampleReg.setCreatedOn(LocalDate.now());
        sampleReg.setDeleted(false);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateRegistration() throws Exception {
        when(registrationService.createRegistration(any())).thenReturn(sampleReg);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(post("/api/registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(sampleReg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationId").value(1));
    }

    @Test
    void testGetRegistrationById_Found() throws Exception {
        when(registrationService.getRegistrationById(1)).thenReturn(sampleReg);

        mockMvc.perform(get("/api/registrations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.eventName").value("TechConf"));
    }

    @Test
    void testGetRegistrationById_NotFound() throws Exception {
        when(registrationService.getRegistrationById(404)).thenReturn(null);

        mockMvc.perform(get("/api/registrations/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllRegistrations() throws Exception {
        when(registrationService.getAllRegistrations()).thenReturn(List.of(sampleReg));

        mockMvc.perform(get("/api/registrations/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.userName").value("JohnDoe"));
    }

    @Test
    void testGetRegistrationsByUserId_Found() throws Exception {
        when(registrationService.getRegistrationsByUserId(1)).thenReturn(List.of(sampleReg));

        mockMvc.perform(get("/api/registrations/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].event.eventId").value(100));
    }

    @Test
    void testGetRegistrationsByUserId_NotFound() throws Exception {
        when(registrationService.getRegistrationsByUserId(404)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/registrations/user/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    void testGetRegistrationsByEventId_Found() throws Exception {
        when(registrationService.getRegistrationsByEventId(100)).thenReturn(List.of(sampleReg));

        mockMvc.perform(get("/api/registrations/event/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user.userName").value("JohnDoe"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    void testGetRegistrationsByEventId_NotFound() throws Exception {
        when(registrationService.getRegistrationsByEventId(999)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/registrations/event/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateRegistration_Found() throws Exception {
        when(registrationService.updateRegistration(any())).thenReturn(sampleReg);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(put("/api/registrations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(sampleReg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value(sampleReg.getUser().getUserId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateRegistration_NotFound() throws Exception {
        when(registrationService.updateRegistration(any())).thenReturn(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(put("/api/registrations/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(sampleReg)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRegistration() throws Exception {
        Mockito.doNothing().when(registrationService).deleteRegistration(1);

        mockMvc.perform(delete("/api/registrations/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration deleted successfully"));
    }
}