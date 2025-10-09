package com.wipro.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.event.entity.Registrations;

public interface RegistrationRepository extends JpaRepository<Registrations, Integer> {
	
	   List<Registrations> findByUserUserId(int userId);
	   List<Registrations> findByEventEventId(int eventId);
	   List<Registrations> findByStatus(String status);

}
