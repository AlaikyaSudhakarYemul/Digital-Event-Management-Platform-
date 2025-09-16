package com.wipro.demp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Getter
@Setter
public class Schedules {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int scheduleId;
    
    private int eventId;
    
    private String session_name;
    
    private String time;
 
}
 