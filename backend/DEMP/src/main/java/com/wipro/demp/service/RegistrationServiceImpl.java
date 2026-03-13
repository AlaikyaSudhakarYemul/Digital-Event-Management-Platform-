package com.wipro.demp.service;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.entity.RegistrationStatus;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.RegistrationRepository;
import com.wipro.demp.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationRepository registrationsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AddressService addressService;
    private final NotificationService notificationService;
    private final JavaMailSender mailSender;

    public RegistrationServiceImpl(RegistrationRepository registrationsRepository,
                                   UserRepository userRepository,
                                   EventRepository eventRepository,
                                   AddressService addressService,
                                   NotificationService notificationService,
                                   JavaMailSender mailSender) {
        this.registrationsRepository = registrationsRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.addressService = addressService;
        this.notificationService = notificationService;
        this.mailSender = mailSender;
    }

    @Override
    public Registrations createRegistration(Registrations registration) {
        // 1) Validate input
        if (registration == null || registration.getUser() == null || registration.getEvent() == null) {
            throw new IllegalArgumentException("User and Event must not be null");
        }

        // 2) Resolve user & event from DB
        Users user = userRepository.findById(registration.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        registration.setUser(user);

        Event event = eventRepository.findById(registration.getEvent().getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        registration.setEvent(event);

        if (event.getActiveStatus() == EventStatus.COMPLETED
            || (event.getDate() != null && event.getDate().isBefore(LocalDate.now()))) {
            throw new IllegalArgumentException("Event is completed. Registration is closed.");
        }

        // 3) Capacity check + optimistic lock save on Event
        if (event.getCurrentAttendees() >= event.getMaxAttendees()) {
            throw new IllegalArgumentException("Event is full, cannot register");
        }
        event.setCurrentAttendees(event.getCurrentAttendees() + 1);
        try {
            eventRepository.saveAndFlush(event);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Event was modified concurrently. Please try again.", e);
        }

        // 4) Fill registration metadata and persist BEFORE notifications
        registration.setCreatedOn(LocalDate.now());
        registration.setCreationTime(LocalDateTime.now());
        registration.setStatus(registration.getStatus() == null
                ? RegistrationStatus.REGISTERED
                : registration.getStatus());
        registration.setDeleted(false);

        registration = registrationsRepository.saveAndFlush(registration);

        // 5) Send confirmation email (plain text)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(safe(registration.getUser().getEmail()));
            message.setSubject("Event Registration Confirmation");

            StringBuilder msg = new StringBuilder();
            msg.append("Dear ").append(safe(registration.getUser().getUserName())).append(",\n\n")
               .append("We’re happy to confirm your registration for the event \"")
               .append(safe(event.getEventName())).append("\".\n\n")
               .append("Here are your registration details:\n")
               .append("Event Name: ").append(safe(event.getEventName())).append("\n")
               .append("Date: ").append(event.getDate() != null ? event.getDate() : "TBA").append("\n")
               .append("Event Type: ").append(event.getEventType() != null ? event.getEventType() : "General").append("\n");

            // Corrected condition: fetch address only if present and id > 0
            if (event.getAddress() != null && event.getAddress().getAddressId() > 0) {
                try {
                    Address addr = addressService.getAddress(event.getAddress().getAddressId());
                    if (addr != null) {
                        msg.append("Address: ")
                           .append(nz(addr.getAddress()))
                           .append(nzComma(addr.getState()))
                           .append(nzComma(addr.getCountry()))
                           .append(nzPincode(addr.getPincode()))
                           .append("\n");
                    }
                } catch (Exception e) {
                    log.warn("Unable to fetch address {} for event {}: {}",
                            event.getAddress().getAddressId(), event.getEventId(), e.getMessage());
                }
            }

            msg.append("\nWe look forward to seeing you there! If you have any questions, feel free to contact our support team.\n\n")
               .append("Thank you for choosing EVENTRA.\n\n")
               .append("Best regards,\n")
               .append("The EVENTRA Team");

            message.setText(msg.toString());
            mailSender.send(message);
            log.info("Confirmation email sent to {}", registration.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email for registration id {}: {}",
                    registration.getRegistrationId(), e.getMessage(), e);
        }

        // 6) Send calendar invite using your existing NotificationService (kept as-is)
        try {
            notificationService.sendCalendarInvite(registration);
            log.info("Calendar invite sent to {}", registration.getUser().getEmail());
        } catch (Exception e) {
            // Do not fail the transaction for email issues; just log
            log.error("Failed to send calendar invite for registration id {}: {}",
                    registration.getRegistrationId(), e.getMessage(), e);
        }

        return registration;
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

    // --------- small helpers to keep email text safe ----------
    private static String safe(String s) { return s == null ? "" : s; }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String nzComma(String s) { return (s == null || s.isBlank()) ? "" : ", " + s; }

    private static String nzPincode(String s) { return (s == null || s.isBlank()) ? "" : " - " + s; }
}