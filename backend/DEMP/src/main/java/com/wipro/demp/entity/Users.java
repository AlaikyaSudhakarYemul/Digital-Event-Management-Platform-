package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 
// import com.eventmanagement.event.model.Event;
// import com.eventmanagement.event.model.Registrations;
import com.fasterxml.jackson.annotation.JsonIgnore;
 
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Getter
@Setter
public class Users {
 
       @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int userId;
 
        @NotNull(message="User name cannot be null")
        private String userName;
       
        @NotNull(message="Password cannot be null")
        private String password;
       
        @Column(unique = true)
        @NotNull(message="Email cannot be null")
        @Email(message = "Invalid email format")
        private String email;
 
        @Enumerated(EnumType.STRING)
        @NotNull(message="Role cannot be null")
        private Role role;
 
        @Size(min = 10, max = 10, message = "Field must be exactly 10 characters")
        private String contactNo;
 
        private LocalDate createdOn;
        private LocalDateTime creationTime;
        private LocalDate updatedOn;
        private LocalDate deletedOn;
        private boolean isDeleted;
 
        //@OneToMany(mappedBy = "user")
        //@JsonIgnore
        //private List<Registrations> registrations;
 
        //@OneToMany(mappedBy = "user")
        //@JsonIgnore
        //private List<Event> events;
 
    }