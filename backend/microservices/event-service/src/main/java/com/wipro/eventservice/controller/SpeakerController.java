package com.wipro.eventservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wipro.eventservice.entity.Speaker;
import com.wipro.eventservice.service.SpeakerService;

@RestController
@RequestMapping("/api/speakers")
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
        if (id < 0) return ResponseEntity.badRequest().build();
        return speakerService.getSpeakerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Speaker createSpeaker(@RequestBody Speaker speaker) {
        if (speaker == null || speaker.getName() == null || speaker.getBio() == null) {
            throw new IllegalArgumentException("Speaker name and bio are required");
        }
        return speakerService.createSpeaker(speaker);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Speaker> updateSpeaker(@PathVariable int id, @RequestBody Speaker speaker) {
        if (id < 0 || speaker == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(speakerService.updateSpeaker(id, speaker));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpeaker(@PathVariable int id) {
        boolean deleted = speakerService.deleteSpeaker(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Speaker deleted successfully");
    }
}
