package com.wipro.admin.service;

import java.util.List;
import java.util.Optional;

import com.wipro.admin.entity.Speaker;

public interface SpeakerService {
 
    List<Speaker> getAllSpeakers();
 
    Optional<Speaker> getSpeakerById(int id);
 
    Speaker createSpeaker(Speaker speaker);
 
    Speaker updateSpeaker(int id, Speaker updatedSpeaker);
 
    boolean deleteSpeaker(int id);
 
    List<Speaker> findAllByIds(List<Integer> ids);
 
}
