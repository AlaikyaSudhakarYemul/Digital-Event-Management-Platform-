package com.wipro.demp.speaker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.wipro.demp.auth.TestConfig;
import com.wipro.demp.controller.SpeakerController;
import com.wipro.demp.entity.Speaker;
import com.wipro.demp.service.SpeakerService;

@WebMvcTest(SpeakerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class SpeakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeakerService speakerService;

    private Speaker speaker() {
        Speaker s = new Speaker();
        s.setSpeakerId(1);
        s.setName("John Doe");
        s.setBio("Senior speaker with extensive conference experience.");
        return s;
    }

    @Test
    void getAllSpeakersSuccess() throws Exception {
        when(speakerService.getAllSpeakers()).thenReturn(List.of(speaker()));

        mockMvc.perform(get("/api/speakers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getSpeakerByIdInvalidId() throws Exception {
        mockMvc.perform(get("/api/speakers/-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSpeakerByIdNotFound() throws Exception {
        when(speakerService.getSpeakerById(5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/speakers/5"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createSpeakerSuccess() throws Exception {
        when(speakerService.createSpeaker(any(Speaker.class))).thenReturn(speaker());

        String payload = """
                {
                  "name": "John Doe",
                  "bio": "Senior speaker with extensive conference experience."
                }
                """;

        mockMvc.perform(post("/api/speakers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.speakerId").value(1));
    }

    @Test
    void updateSpeakerBadRequestForInvalidBody() throws Exception {
        String payload = """
                {
                  "name": null,
                  "bio": null
                }
                """;

        mockMvc.perform(put("/api/speakers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSpeakerInvalidId() throws Exception {
        mockMvc.perform(delete("/api/speakers/-10"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid speaker ID."));
    }

    @Test
    void deleteSpeakerReturnsOkMessage() throws Exception {
        mockMvc.perform(delete("/api/speakers/10"))
            .andExpect(status().isOk())
            .andExpect(content().string("Speaker deleted successfully!"));
    }
}
