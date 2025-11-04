package com.wipro.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.wipro.event.dto.RegistrationsDTO;
import com.wipro.event.entity.Address;
import com.wipro.event.entity.Event;
import com.wipro.event.entity.RegistrationStatus;
import com.wipro.event.entity.Registrations;
import com.wipro.event.entity.Users;
import com.wipro.event.repository.EventRepository;
import com.wipro.event.repository.RegistrationRepository;

import jakarta.persistence.OptimisticLockException;

@Service
public class RegistrationServiceImpl implements RegistrationService {
	
	private final RegistrationRepository registrationRepository;
	private final EventRepository eventRepository;
	private final EventServiceImpl eventService;

	@Autowired
    private JavaMailSender mailSender;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public RegistrationServiceImpl(RegistrationRepository registrationRepository, EventRepository eventRepository,EventServiceImpl eventService) {
		this.registrationRepository = registrationRepository;
		this.eventRepository = eventRepository;
		this.eventService = eventService;
	}
	
	public Users getUserById(int userId){
        String url = "http://DEMP/api/user/" + userId;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("No request context available for forwarding JWT token");
        }
        jakarta.servlet.http.HttpServletRequest request = (jakarta.servlet.http.HttpServletRequest) ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
        String authHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Users> response = restTemplate.exchange(url, HttpMethod.GET, entity, Users.class);
        return response.getBody();
    }
	
	public RegistrationsDTO getRegistrationDTO(Registrations registrations) {
		RegistrationsDTO registrationsDTO = new RegistrationsDTO();
		registrationsDTO.setRegistrationId(registrations.getRegistrationId());
		registrationsDTO.setUserId(registrations.getUserId());
		registrationsDTO.setUser(getUserById(registrations.getUserId()));
		registrationsDTO.setEvent(registrations.getEvent());
		registrationsDTO.setStatus(registrations.getStatus());
		registrationsDTO.setCreatedOn(registrations.getCreatedOn());
		registrationsDTO.setCreationTime(registrations.getCreationTime());
		registrationsDTO.setUpdatedOn(registrations.getUpdatedOn());
		registrationsDTO.setDeletedOn(registrations.getDeletedOn());
		registrationsDTO.setDeleted(registrations.isDeleted());
		return registrationsDTO;
	}
	
	/*public Registrations getRegistrations(RegistrationsDTO registrationsDTO) {
		Registrations registrations = new Registrations();
		registrations.setRegistrationId(registrationsDTO.getRegistrationId());
		registrations.setUserId(registrationsDTO.getUserId());
		registrations.setEvent(registrationsDTO.getEvent());
		registrations.setStatus(registrationsDTO.getStatus());
		registrations.setCreatedOn(registrationsDTO.getCreatedOn());
		registrations.setCreationTime(registrationsDTO.getCreationTime());
		registrations.setUpdatedOn(registrationsDTO.getUpdatedOn());
		registrations.setDeletedOn(registrationsDTO.getDeletedOn());
		registrations.setDeleted(registrationsDTO.isDeleted());
		return registrations;
	}*/
	

	@Override
	public RegistrationsDTO createRegistration(Registrations registration) {
		
		   if (registration.getUserId()<=0 || registration.getEvent() == null) {
	            throw new IllegalArgumentException("User ID should be valid and Event must not be null");
	        }

//	        Users user = getUserById(registration.getUserId());

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
	            registration.getStatus() == null ? RegistrationStatus
	            		.REGISTERED : registration.getStatus());

	        registration.setDeleted(false);
	        Registrations registrations = registrationRepository.save(registration);
	        RegistrationsDTO regDTO = getRegistrationDTO(registrations);
	        
			SimpleMailMessage message = new SimpleMailMessage();
			
        	message.setTo(regDTO.getUser().getEmail());
        	message.setSubject("Event Registration Confirmation");
        	
        	String msg = "Dear " + regDTO.getUser().getUserName() + ",\n\n" +
                    "We’re happy to confirm your registration for the event *" + regDTO.getEvent().getEventName() + "!* 🎉\n\n" +
                    "Here are your registration details:\n" +
                    "\n" +
                    "*Event Name:* " + regDTO.getEvent().getEventName() + "\n" +
                    "*Date:* " + regDTO.getEvent().getDate() +  "\n" +
                    "*Event Type:* " + regDTO.getEvent().getEventType() + "\n" ;
        	if(regDTO.getEvent().getAddressId()!=null) {
        		Address address = eventService.getAddressById(regDTO.getEvent().getAddressId());	
        		msg+="*Address:* "+address.getAddress()+", "+address.getState()+", "+address.getCountry()+" - "+address.getPincode()+"\n\n";
        	}
        	
            msg += "\nWe look forward to seeing you there! If you have any questions or need assistance, feel free to contact our support team.\n\n" +
                    "Thank you for choosing *EVENTRA*.\n\n" +
                    "Best regards,\n" +
                    "The EVENTRA Team";
       
        	message.setText(msg);
        	mailSender.send(message);
        	
	        return regDTO;
		
	}

	@Override
	public RegistrationsDTO getRegistrationById(int id) {
		
		Registrations registration = registrationRepository.findById(id).orElse(null);
		
		
		return getRegistrationDTO(registration);
	}

	@Override
	public List<RegistrationsDTO> getAllRegistrations() {
		
		List<Registrations> registrationsList = registrationRepository.findAll();
		
		List<RegistrationsDTO> registrationsDTOs = new ArrayList<>();
		
		for(Registrations registration : registrationsList) {
			registrationsDTOs.add(getRegistrationDTO(registration));
		}
		
		return registrationsDTOs;
	}

	@Override
	public List<RegistrationsDTO> getRegistrationsByUserId(int userId) {
	    List<Registrations> registrationsList = registrationRepository.findByUserId(userId);
	    List<RegistrationsDTO> registrationsDTOs = new ArrayList<>();
	    for (Registrations registration : registrationsList) {
	        registrationsDTOs.add(getRegistrationDTO(registration));
	    }
	    return registrationsDTOs;
	}

	@Override
	public List<RegistrationsDTO> getRegistrationsByEventId(int eventId) {
		
		List<Registrations> registrationsList =  registrationRepository.findByEventEventId(eventId);
		
		List<RegistrationsDTO> registrationsDTOs = new ArrayList<>();
		
		for(Registrations registration : registrationsList) {
			registrationsDTOs.add(getRegistrationDTO(registration));
		}
		
		return registrationsDTOs;
	}

	@Override
	public RegistrationsDTO updateRegistration(Registrations registration) {
		
		Optional<Registrations> existing = registrationRepository.findById(registration.getRegistrationId());
        if (registration.getUserId() <=0 || registration.getEvent() == null) {
            throw new IllegalArgumentException("User ID should be valid and Event must not be null");
        }

//        Users user = userRepository.findById(registration.getUser().getUserId())
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        registration.setUser(user);

        Event event = eventRepository.findById(registration.getEvent().getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        registration.setEvent(event);

        if (existing.isPresent()) {
            Registrations existingReg = existing.get();
            registration.setCreatedOn(existingReg.getCreatedOn());
            registration.setUpdatedOn(LocalDate.now());
            registration.setStatus(RegistrationStatus.UPDATED);
            Registrations updatedReg =  registrationRepository.save(registration);
            return getRegistrationDTO(updatedReg);
        }
        return null;
	}

	@Override
	public void deleteRegistration(int id) {
		
		 Optional<Registrations> reg = registrationRepository.findById(id);
	        reg.ifPresent(r -> {
	            r.setDeletedOn(LocalDate.now());
	            r.setDeleted(true);
	            r.setStatus(RegistrationStatus.CANCELLED);
	            registrationRepository.save(r);
	        });
		
	}

}
