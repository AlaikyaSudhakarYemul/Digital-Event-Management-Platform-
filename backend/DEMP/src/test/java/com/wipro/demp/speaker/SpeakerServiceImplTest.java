package com.wipro.demp.speaker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wipro.demp.entity.Speaker;
import com.wipro.demp.repository.SpeakerRepository;
import com.wipro.demp.service.SpeakerServiceImpl;

@ExtendWith(MockitoExtension.class)
class SpeakerServiceImplTest {

    @Mock
    private SpeakerRepository speakerRepository;

    @InjectMocks
    private SpeakerServiceImpl speakerService;

    private Speaker speaker(String name, String bio) {
        Speaker s = new Speaker();
        s.setName(name);
        s.setBio(bio);
        return s;
    }

    @Test
    void createSpeakerSetsAuditAndSaves() {
        Speaker input = speaker("Akhil", "Experienced speaker with deep domain knowledge");
        when(speakerRepository.save(input)).thenReturn(input);

        Speaker result = speakerService.createSpeaker(input);

        assertNotNull(result.getCreatedOn());
        assertNotNull(result.getCreationTime());
        verify(speakerRepository).save(input);
    }

    @Test
    void updateSpeakerSuccess() {
        Speaker existing = speaker("Akhil", "Old bio with enough characters");
        Speaker update = speaker("Akhil Updated", "New bio with enough characters");

        when(speakerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(speakerRepository.save(existing)).thenReturn(existing);

        Speaker result = speakerService.updateSpeaker(1, update);

        assertEquals("Akhil Updated", result.getName());
        assertEquals("New bio with enough characters", result.getBio());
    }

    @Test
    void updateSpeakerNotFoundThrows() {
        when(speakerRepository.findById(55)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> speakerService.updateSpeaker(55, speaker("N", "Bio with enough chars")));

        assertEquals("Speaker not found with id: 55", ex.getMessage());
    }

    @Test
    void deleteSpeakerSuccess() {
        Speaker existing = speaker("Akhil", "Bio with enough chars");
        when(speakerRepository.existsById(2)).thenReturn(true);
        when(speakerRepository.findById(2)).thenReturn(Optional.of(existing));

        boolean result = speakerService.deleteSpeaker(2);

        assertEquals(true, result);
        assertEquals(true, existing.isDeleted());
        verify(speakerRepository).save(existing);
    }

    @Test
    void deleteSpeakerNotFoundThrows() {
        when(speakerRepository.existsById(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> speakerService.deleteSpeaker(99));

        assertEquals("Speaker not found with id: 99", ex.getMessage());
    }
}
