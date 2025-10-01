package com.wipro.demp.dto;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.entity.EventType;
import com.wipro.demp.entity.Speaker;
import com.wipro.demp.entity.Users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class EventDTO {
    private int eventId;
    private String eventName;
    private String description;
    private LocalDate date;
    private String image;
    private EventType eventType;
    private Integer addressId;
    private Address address; 
    private List<Integer> speakerIds;
    private List<Speaker> speakers;
    private Users user;
    private EventStatus activeStatus;
    private int maxAttendees;
    private int currentAttendees;
    private LocalDateTime creationTime;
    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;
    private Integer version;

}
