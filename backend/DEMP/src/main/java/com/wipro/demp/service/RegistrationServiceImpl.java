package com.wipro.demp.service;


import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.RegistrationStatus;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.RegistrationRepository;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public RegistrationServiceImpl(RegistrationRepository registrationsRepository, UserRepository userRepository,
            EventRepository eventRepository) {
        this.registrationsRepository = registrationsRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Registrations createRegistration(Registrations registration) {

        if (registration.getUser() == null || registration.getEvent() == null) {
            throw new IllegalArgumentException("User and Event must not be null");
        }

        Users user = userRepository.findById(registration.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        registration.setUser(user);

        Event event = eventRepository.findById(registration.getEvent().getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        registration.setEvent(event);

        if (event.getCurrentAttendees() >= event.getMaxAttendees()) {
            throw new IllegalArgumentException("Event is full, cannot register");
        }
        event.setCurrentAttendees(event.getCurrentAttendees() + 1);
        try {
            eventRepository.saveAndFlush(event); 
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Event was modified concurrently. Please try again.");
        }

        // eventRepository.save(event);

        registration.setCreatedOn(LocalDate.now());
        registration.setCreationTime(LocalDateTime.now());
        registration.setStatus(
            registration.getStatus() == null ? RegistrationStatus.REGISTERED : registration.getStatus());

        registration.setDeleted(false);
        return registrationsRepository.save(registration);
    }

    @Override
    public Registrations getRegistrationById(int id) {
        return registrationsRepository.findById(id).orElse(null);
    }

    @Override
    public List<Registrations> getAllRegistrations() {
        return registrationsRepository.findAll();
    }

    @Override
    public List<Registrations> getRegistrationsByUserId(int userId) {
        return registrationsRepository.findByUserUserId(userId);
    }

    @Override
    public List<Registrations> getRegistrationsByEventId(int eventId) {
        return registrationsRepository.findByEventEventId(eventId);
    }

    @Override
    public Registrations updateRegistration(Registrations registration) {
        Optional<Registrations> existing = registrationsRepository.findById(registration.getRegistrationId());
        if (registration.getUser() == null || registration.getEvent() == null) {
            throw new IllegalArgumentException("User and Event must not be null");
        }

        Users user = userRepository.findById(registration.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        registration.setUser(user);

        Event event = eventRepository.findById(registration.getEvent().getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        registration.setEvent(event);

        if (existing.isPresent()) {
            Registrations existingReg = existing.get();
            registration.setCreatedOn(existingReg.getCreatedOn());
            registration.setUpdatedOn(LocalDate.now());
            registration.setStatus(RegistrationStatus.UPDATED);
            return registrationsRepository.save(registration);
        }
        return null;
    }

    @Override
    public void deleteRegistration(int id) {
        Optional<Registrations> reg = registrationsRepository.findById(id);
        reg.ifPresent(r -> {
            r.setDeletedOn(LocalDate.now());
            r.setDeleted(true);
            r.setStatus(RegistrationStatus.CANCELLED);
            registrationsRepository.save(r);
        });
    }

}