package com.wipro.demp.service;

import java.util.List;
import java.util.Optional;
 
import com.wipro.demp.entity.Speaker;
 
public interface SpeakerService {
 
    List<Speaker> getAllSpeakers();
 
    Optional<Speaker> getSpeakerById(int id);
 
    Speaker createSpeaker(Speaker speaker);
 
    Speaker updateSpeaker(int id, Speaker updatedSpeaker);
 
    boolean deleteSpeaker(int id);
 
    List<Speaker> findAllByIds(List<Integer> ids);
 
}