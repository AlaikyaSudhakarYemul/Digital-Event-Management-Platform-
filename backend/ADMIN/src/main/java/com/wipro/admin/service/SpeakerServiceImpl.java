package com.wipro.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wipro.admin.entity.Speaker;
import com.wipro.admin.exception.SpeakerNotFoundException;
import com.wipro.admin.repository.SpeakerRepository;

@Service
public class SpeakerServiceImpl implements SpeakerService {
 
    private final SpeakerRepository speakerRepository;
 
    public SpeakerServiceImpl(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }
 
    @Override
    public List<Speaker> getAllSpeakers() {
        return speakerRepository.findAll();
    }
 
    @Override
    public Optional<Speaker> getSpeakerById(int id) {
        Optional<Speaker> speakerOpt = speakerRepository.findById(id);
        if (speakerOpt.isEmpty()) {
            throw new SpeakerNotFoundException("Speaker not found with id: " + id);
        }
        return speakerOpt;
    }
 
    @Override
    public Speaker createSpeaker(Speaker speaker) {
        speaker.setCreatedOn(LocalDate.now());
        speaker.setCreationTime(LocalDateTime.now());
        speaker.setUpdatedOn(LocalDate.now());
        speaker.setDeleted(false);
        return speakerRepository.save(speaker);
    }
 
    @Override
    public Speaker updateSpeaker(int id, Speaker updatedSpeaker) {
        return speakerRepository.findById(id).map(speaker -> {
            speaker.setName(updatedSpeaker.getName());
            speaker.setBio(updatedSpeaker.getBio());
            speaker.setUpdatedOn(LocalDate.now());
            return speakerRepository.save(speaker);
        }).orElseThrow(() -> new SpeakerNotFoundException("Speaker not found with id: " + id));
    }
 
    @Override
    public boolean deleteSpeaker(int id) {
        if (speakerRepository.existsById(id)) {
            Optional<Speaker> speakerOpt = speakerRepository.findById(id);
            if (speakerOpt.isPresent()) {   
                Speaker speaker = speakerOpt.get();
                speaker.setDeletedOn(LocalDate.now());
                speaker.setDeleted(true);
                speakerRepository.save(speaker);
            }
            return true;
        }
        throw new SpeakerNotFoundException("Speaker not found with id: " + id);
    }
 
    @Override
    public List<Speaker> findAllByIds(List<Integer> ids) {
        List<Speaker> speakers = speakerRepository.findAllById(ids);
        if (speakers.size() != ids.size()) {
            throw new SpeakerNotFoundException("One or more speaker IDs are invalid.");
        }
        return speakers;
    }
}