package com.wipro.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.wipro.event.entity.Address;
import com.wipro.event.entity.EventStatus;
import com.wipro.event.entity.EventType;
import com.wipro.event.entity.Speaker;
import com.wipro.event.entity.Users;


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
