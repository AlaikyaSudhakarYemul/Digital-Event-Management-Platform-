package com.wipro.event.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Users {
 
        private int userId;
 
        private String userName;
       
        private String password;
       
        private String email;
 
        private Role role;
 
        private String contactNo;
 
        private LocalDate createdOn;
        private LocalDateTime creationTime;
        private LocalDate updatedOn;
        private LocalDate deletedOn;
        private boolean isDeleted;
 
        //@OneToMany(mappedBy = "user")
        //@JsonIgnore
        //private List<Registrations> registrations;
 
        // @OneToMany(mappedBy = "user")
        // @JsonIgnore
        // private List<Event> events;
 
    }