package com.wipro.registrationservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wipro.registrationservice.entity.Registrations;

public interface RegistrationRepository extends JpaRepository<Registrations, Integer> {

    List<Registrations> findByUserId(int userId);

    List<Registrations> findByEventId(int eventId);

    @Query("select case when count(r) > 0 then true else false end from Registrations r where r.userId = :userId and r.eventId = :eventId and r.isDeleted = false")
    boolean existsActiveRegistration(@Param("userId") int userId, @Param("eventId") int eventId);
}
