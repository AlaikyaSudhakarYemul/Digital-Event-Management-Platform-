package com.wipro.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.wipro.event.dto.RegistrationsDTO;
import com.wipro.event.entity.Event;
import com.wipro.event.entity.RegistrationStatus;
import com.wipro.event.entity.Registrations;
import com.wipro.event.entity.Users;
import com.wipro.event.repository.EventRepository;
import com.wipro.event.repository.RegistrationRepository;

import jakarta.persistence.OptimisticLockException;

public class RegistrationServiceImpl implements RegistrationService {
	
	private final RegistrationRepository registrationRepository;
	private final EventRepository eventRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public RegistrationServiceImpl(RegistrationRepository registrationRepository, EventRepository eventRepository) {
		this.registrationRepository = registrationRepository;
		this.eventRepository = eventRepository;
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
	            throw new IllegalArgumentException("User ID should be valud and Event must not be null");
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
	        return getRegistrationDTO(registrations);
		
	}

	@Override
	public RegistrationsDTO getRegistrationById(int id) {
		
		return null;
	}

	@Override
	public List<RegistrationsDTO> getAllRegistrations() {
		
		return null;
	}

	@Override
	public List<RegistrationsDTO> getRegistrationsByUserId(int userId) {
		
		return null;
	}

	@Override
	public List<RegistrationsDTO> getRegistrationsByEventId(int eventId) {
		
		return null;
	}

	@Override
	public RegistrationsDTO updateRegistration(Registrations registration) {
		
		return null;
	}

	@Override
	public void deleteRegistration(int id) {
		
		
	}

}
