package com.wipro.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.admin.constants.AdminServiceConstants;
import com.wipro.admin.entity.Speaker;
import com.wipro.admin.service.SpeakerService;

@RestController
@RequestMapping(AdminServiceConstants.API_URL + AdminServiceConstants.SPEAKERS_URL)
@CrossOrigin(origins = AdminServiceConstants.FRONTEND_URL)
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    public List<Speaker> getAllSpeakers() {
        return speakerService.getAllSpeakers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Speaker> getSpeakerById(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().build();
        }

        return speakerService.getSpeakerById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Speaker createSpeaker(@RequestBody Speaker speaker) {
        if (speaker == null || speaker.getName() == null || speaker.getBio() == null) {
            throw new IllegalArgumentException("Speaker name and bio are required");
        }

        return speakerService.createSpeaker(speaker);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Speaker> updateSpeaker(@PathVariable int id, @RequestBody Speaker speaker) {
        if (id < 0 || speaker == null || speaker.getName() == null || speaker.getBio() == null) {
            return ResponseEntity.badRequest().build();
        }

        Speaker updated = speakerService.updateSpeaker(id, speaker);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpeaker(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().body("Invalid speaker ID.");
        }

        speakerService.deleteSpeaker(id);
        return ResponseEntity.ok("Speaker deleted successfully!");
    }
}
