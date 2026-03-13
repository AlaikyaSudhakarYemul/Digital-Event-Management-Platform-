package com.wipro.demp.repository;

import com.wipro.demp.entity.Registrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
 
 
public interface RegistrationRepository extends JpaRepository<Registrations, Integer> {
   List<Registrations> findByUserUserId(int userId);
   List<Registrations> findByEventEventId(int eventId);
   List<Registrations> findByStatus(String status);

   @Query("select case when count(r) > 0 then true else false end from Registrations r where r.user.userId = :userId and r.event.eventId = :eventId and r.isDeleted = false")
   boolean existsActiveRegistration(@Param("userId") int userId, @Param("eventId") int eventId);
}