package com.wipro.eventservice.service;

import java.util.List;
import java.util.Optional;

import com.wipro.eventservice.entity.Speaker;

public interface SpeakerService {
    List<Speaker> getAllSpeakers();
    Optional<Speaker> getSpeakerById(int id);
    Speaker createSpeaker(Speaker speaker);
    Speaker updateSpeaker(int id, Speaker speaker);
    boolean deleteSpeaker(int id);
    List<Speaker> findAllByIds(List<Integer> ids);
}
