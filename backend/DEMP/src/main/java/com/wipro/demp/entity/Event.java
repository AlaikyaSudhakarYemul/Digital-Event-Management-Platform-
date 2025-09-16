package com.wipro.demp.entity;


 
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class Event {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventId;
 
    @NotNull(message = "Event name cannot be NULL")
    private String eventName;
 
    @NotNull(message = "Event description cannot be NULL")
    @Size(min=10, max = 100, message = "Event description must be more than 10 characters and less than or equal to 100 characters")
    private String description;
 
    @NotNull(message = "Date must not be NULL")
    private LocalDate date;
 
    // saving the image url or path
    private String image;
 
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Event type cannot be null")
    private EventType eventType;
 
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
 
 
    
 
    @ManyToMany
    @JoinTable(name = "event_speaker", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "speaker_id"))
    private List<Speaker> speakers;
 
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
 
    @Enumerated(EnumType.STRING)
    private EventStatus activeStatus;
 
    @NotNull(message = "Max Attendees cannot be NULL")
    @Min(value=10,message="Attendees must be more than 10")
    @Max(value=500,message="Attendees must be less than or equal to 500")
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
 
