package com.wipro.event.service;

import java.util.List;

import com.wipro.event.dto.RegistrationsDTO;
import com.wipro.event.entity.Registrations;

public interface RegistrationService {
	
	RegistrationsDTO createRegistration(Registrations registration);

	RegistrationsDTO getRegistrationById(int id);

	List<RegistrationsDTO> getAllRegistrations();

	List<RegistrationsDTO> getRegistrationsByUserId(int userId);

	List<RegistrationsDTO> getRegistrationsByEventId(int eventId);

	RegistrationsDTO updateRegistration(Registrations registration);

	void deleteRegistration(int id);
	
}