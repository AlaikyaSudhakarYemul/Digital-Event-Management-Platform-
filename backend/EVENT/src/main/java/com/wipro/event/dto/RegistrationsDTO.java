package com.wipro.event.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.wipro.event.entity.Event;
import com.wipro.event.entity.RegistrationStatus;
import com.wipro.event.entity.Users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationsDTO {
	
	private int registrationId;

    private int userId;
    private Users user;

    private Event event;
    private RegistrationStatus status;
    private LocalDate createdOn;
    private LocalDateTime creationTime;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;

}
