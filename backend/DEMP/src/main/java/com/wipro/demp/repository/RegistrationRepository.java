package com.wipro.demp.repository;

import com.wipro.demp.entity.Registrations;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
 
public interface RegistrationRepository extends JpaRepository<Registrations, Integer> {
   List<Registrations> findByUserUserId(int userId);
   List<Registrations> findByEventEventId(int eventId);
   List<Registrations> findByStatus(String status);
}