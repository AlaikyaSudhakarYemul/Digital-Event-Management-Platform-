package com.wipro.admin.controller;

import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.admin.entity.Speaker;
import com.wipro.admin.service.SpeakerService;
 
@RestController
@RequestMapping("/api/speakers")
@CrossOrigin(origins="http://localhost:3000")
public class SpeakerController {
 
    private static final Logger logger = LoggerFactory.getLogger(SpeakerController.class);
 
    @Autowired
    private SpeakerService speakerService;
 
    @GetMapping
    public List<Speaker> getAllSpeakers() {
        logger.info("Fetching all speakers");
        if (speakerService.getAllSpeakers().isEmpty()) {
            logger.warn("No speakers found");
        }
        logger.info("Total speakers found: {}", speakerService.getAllSpeakers().size());
        return speakerService.getAllSpeakers();
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<Speaker> getSpeakerById(@PathVariable int id) {
        logger.info("Fetching speaker with ID: {}", id);
        if (id < 0) {
            logger.error("Invalid ID: {}", id);
            return ResponseEntity.badRequest().build();
        }
        logger.info("Retrieving speaker with ID: {}", id);
        return speakerService.getSpeakerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Speaker createSpeaker(@RequestBody Speaker speaker) {
        logger.info("Creating speaker: {}", speaker);
        if (speaker == null || speaker.getName() == null || speaker.getBio() == null) {
            logger.error("Invalid speaker data: {}", speaker);
            throw new IllegalArgumentException("Speaker name and bio are required");
        }
        logger.info("Speaker data is valid, proceeding to create speaker");
        logger.info("Creating speaker in the service");
        logger.info("Calling speakerService to create speaker");
        logger.debug("Speaker details: {}", speaker);
        return speakerService.createSpeaker(speaker);
    }
  
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Speaker> updateSpeaker(@PathVariable int id, @RequestBody Speaker speaker) {
        logger.info("Updating speaker with ID: {}", id);
        if (id < 0 || speaker == null || speaker.getName() == null || speaker.getBio() == null) {
            logger.error("Invalid speaker data for ID {}: {}", id, speaker);
            return ResponseEntity.badRequest().build();
        }
        Speaker updated = speakerService.updateSpeaker(id, speaker);
        logger.info("Speaker with ID {} updated successfully: {}", id, updated);
        if (updated == null) {
            logger.warn("Speaker with ID {} not found for update", id);
        }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpeaker(@PathVariable int id) {
        logger.info("Deleting speaker with ID: {}", id);
        
        if (id < 0) {
            logger.error("Invalid ID for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid speaker ID.");
        }
        logger.info("Calling speakerService to delete speaker with ID: {}", id);
        logger.debug("Speaker ID to delete: {}", id);
        
        logger.info("Speaker with ID {} deleted successfully", id);
        return ResponseEntity.ok("Speaker deleted successfully!");
    }
        
}
