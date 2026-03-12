package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Getter
@Setter
public class CopiedEvents {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int copiedEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;
 
    private String eventName;
 
    private String description;
 
    private LocalDate date;

    private LocalTime time;
 
    private String image;
 
    @Enumerated(EnumType.STRING)
    private EventType eventType;
 
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "copied_event_speaker",
        joinColumns = @JoinColumn(name = "copied_event_id"),
        inverseJoinColumns = @JoinColumn(name = "speaker_id"))
    private List<Speaker> speakers;
 
    @ManyToOne
    @JoinColumn(name = "user_id")
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

    @JsonProperty("eventId")
    public Integer getEventId() {
        return event != null ? event.getEventId() : null;
    }

    @JsonProperty("eventId")
    public void setEventId(Integer eventId) {
        if (eventId == null) {
            this.event = null;
            return;
        }
        Event eventRef = new Event();
        eventRef.setEventId(eventId);
        this.event = eventRef;
    }
}
 
