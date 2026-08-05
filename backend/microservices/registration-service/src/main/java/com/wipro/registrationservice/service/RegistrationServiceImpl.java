package com.wipro.registrationservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wipro.registrationservice.entity.RegistrationStatus;
import com.wipro.registrationservice.entity.Registrations;
import com.wipro.registrationservice.repository.RegistrationRepository;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationRepository registrationRepository;
    private final JavaMailSender mailSender;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, JavaMailSender mailSender) {
        this.registrationRepository = registrationRepository;
        this.mailSender = mailSender;
    }

    @Override
    public Registrations createRegistration(int userId, int eventId) {
        if (registrationRepository.existsActiveRegistration(userId, eventId)) {
            throw new IllegalArgumentException("You are already registered for this event.");
        }

        Registrations registration = new Registrations();
        registration.setUserId(userId);
        registration.setEventId(eventId);
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setCreatedOn(LocalDate.now());
        registration.setCreationTime(LocalDateTime.now());
        registration.setUpdatedOn(LocalDate.now());
        registration.setDeleted(false);

        Registrations saved = registrationRepository.save(registration);
        log.info("Registration {} created for user {} and event {}", saved.getRegistrationId(), userId, eventId);
        return saved;
    }

    @Override
    public Registrations getRegistrationById(int id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
    }

    @Override
    public List<Registrations> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Override
    public List<Registrations> getRegistrationsByUserId(int userId) {
        return registrationRepository.findByUserId(userId);
    }

    @Override
    public List<Registrations> getRegistrationsByEventId(int eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Override
    public void cancelRegistration(int id) {
        Registrations registration = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setDeleted(true);
        registration.setDeletedOn(LocalDate.now());
        registrationRepository.save(registration);
    }
}
