package com.wipro.registrationservice.service;

import java.util.List;

import com.wipro.registrationservice.entity.Registrations;

public interface RegistrationService {
    Registrations createRegistration(int userId, int eventId);
    Registrations getRegistrationById(int id);
    List<Registrations> getAllRegistrations();
    List<Registrations> getRegistrationsByUserId(int userId);
    List<Registrations> getRegistrationsByEventId(int eventId);
    void cancelRegistration(int id);
}
