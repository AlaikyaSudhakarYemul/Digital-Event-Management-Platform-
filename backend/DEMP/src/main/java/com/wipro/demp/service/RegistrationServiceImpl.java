package com.wipro.demp.service;


import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.RegistrationStatus;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.repository.AddressRepository;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.RegistrationRepository;
import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final RegistrationRepository registrationsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
	private JavaMailSender mailSender;

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

        SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(registration.getUser().getEmail());
		message.setSubject("Event Registration Confirmation");

		String msg = "Dear " + registration.getUser().getUserName() + ",\n\n" +
				"We’re happy to confirm your registration for the event *" + registration.getEvent().getEventName()
				+ "!* 🎉\n\n" +
				"Here are your registration details:\n" +
				"\n" +
				"*Event Name:* " + registration.getEvent().getEventName() + "\n" +
				"*Date:* " + registration.getEvent().getDate() + "\n" +
				"*Event Type:* " + registration.getEvent().getEventType() + "\n";
                

		if (registration.getEvent().getAddress().getAddressId() <= 0) {
			Address address = addressService.getAddress(registration.getEvent().getAddress().getAddressId());
			msg += "*Address:* " + address.getAddress() + ", " + address.getState() + ", " + address.getCountry()
					+ " - " + address.getPincode() + "\n\n";
		}

        msg += "\nWe look forward to seeing you there! If you have any questions or need assistance, feel free to contact our support team.\n\n"
				+
				"Thank you for choosing *EVENTRA*.\n\n" +
				"Best regards,\n" +
				"The EVENTRA Team";
				

		message.setText(msg);
		mailSender.send(message);

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