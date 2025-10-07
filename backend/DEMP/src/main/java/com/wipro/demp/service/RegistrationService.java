package com.wipro.demp.service;

import java.util.List;

import com.wipro.demp.entity.Registrations;


    public interface RegistrationService {
   Registrations createRegistration(Registrations registration);
   Registrations getRegistrationById(int id);
   List<Registrations> getAllRegistrations();
   List<Registrations> getRegistrationsByUserId(int userId);
   List<Registrations> getRegistrationsByEventId(int eventId);
   Registrations updateRegistration(Registrations registration);
   void deleteRegistration(int id);
}

