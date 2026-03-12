package com.wipro.demp.controller;

import java.security.InvalidParameterException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wipro.demp.entity.CopiedEvents;
import com.wipro.demp.service.RegistrationService;
import com.wipro.demp.service.CopiedEventsService;
 
@RestController
@RequestMapping("/api/copied/events")
@CrossOrigin(origins = "http://localhost:3000")
public class CopiedEventsController {
 
    private final CopiedEventsService copiedeventsService;
 
    public CopiedEventsController(CopiedEventsService copiedeventsService, RegistrationService registrationService) {
        this.copiedeventsService = copiedeventsService;
    }
 
    @PostMapping("/create")
    public ResponseEntity<?> createCopiedEvents(@RequestBody CopiedEvents copiedevents) {
        CopiedEvents createdCopiedEvent = copiedeventsService.createCopiedEvents(copiedevents);
        if (createdCopiedEvent == null) {
            
            return ResponseEntity.badRequest().body("Invalid event data.");
        }
        return new ResponseEntity<>(createdCopiedEvent, HttpStatus.CREATED);
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<?> getCopiedEventsById(@PathVariable int id) {
        
        if (id < 0) {
           
            throw new InvalidParameterException("ID must be a positive integer");
        }
        
        CopiedEvents copiedevents = copiedeventsService.getCopiedEventsById(id);
        
        return new ResponseEntity<>(copiedevents, HttpStatus.OK);
    }
 
    @GetMapping("/all")
    public ResponseEntity<?> getAllCopiedEvents() {
        return new ResponseEntity<>(copiedeventsService.getAllCopiedEvents(), HttpStatus.OK);
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCopiedEvents(@PathVariable int id, @RequestBody CopiedEvents updatedCopiedEvent) {
        
        if (id < 0 || updatedCopiedEvent == null) {
            
            return ResponseEntity.badRequest().body("Invalid request body.");
        }
        CopiedEvents copiedevents = copiedeventsService.updateCopiedEvents(id, updatedCopiedEvent);
        
        return new ResponseEntity<>(copiedevents, HttpStatus.OK);
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCopiedEvents(@PathVariable int id) {
        
        if (id < 0) {
            
            throw new InvalidParameterException("ID must be a positive integer");
        }
        copiedeventsService.deleteCopiedEvents(id);
        
        return ResponseEntity.ok("Copied events deleted successfully");
    }
 
    @GetMapping("/search")
    public ResponseEntity<?> searchCopiedEvents(@RequestParam String eventName) {
        
        if (eventName == null || eventName.isEmpty()) {
            
            return ResponseEntity.badRequest().body("Invalid CopiedEvent name.");
        }
        
        return new ResponseEntity<>(copiedeventsService.findByCopiedEventsName(eventName), HttpStatus.OK);
    }
 
}