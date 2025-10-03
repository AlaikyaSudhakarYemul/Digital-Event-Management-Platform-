package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Speaker {

    private int speakerId;
 
    private String name;
 
    private String bio;
 
    // @ManyToMany(mappedBy = "speakers")
    // @JsonIgnore
    // private List<Event> events;
 
    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private LocalDateTime creationTime;
    private boolean isDeleted;
 
}
