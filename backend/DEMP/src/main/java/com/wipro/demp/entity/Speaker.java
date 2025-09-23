package com.wipro.demp.entity;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
 
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Getter
@Setter
public class Speaker {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int speakerId;
 
    @NotNull(message = "Speaker name cannot be NULL")
    @Size(min = 1, max = 50, message = "Speaker name must be more than 1 character and less than or equal to 50 characters")
    private String name;
 
    @NotNull(message = "Speaker bio cannot be NULL")
    @Size(min = 10, max = 100, message = "Speaker bio must be more than 10 characters and less than or equal to 100 characters")
    private String bio;
 
    @ManyToMany(mappedBy = "speakers")
    @JsonIgnore
    private List<Event> events;
 
    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private LocalDateTime creationTime;
    private boolean isDeleted;
 
}
 