package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
 
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Getter
@Setter
public class CopiedEvents {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int copiedEventId;
    
    private int eventId;
 
    private String eventName;
 
    private String description;
 
    private LocalDate date;

    private LocalTime time;
 
    private String image;
 
    private EventType eventType;
 
    private Address address;
    
    private List<Speaker> speakers;
 
    private Users user;
 
    @Enumerated(EnumType.STRING)
    private EventStatus activeStatus;

    private int maxAttendees;
 
    private int currentAttendees;
 
    private LocalDateTime creationTime;
    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private boolean isDeleted;
 
    @Version
    private Integer version;
}
 
