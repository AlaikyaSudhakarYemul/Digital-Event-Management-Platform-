package com.wipro.event.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Address {

    private int addressId;

    private String address;

    private String state;
    private String country;

    private String pincode;

    // @OneToMany(mappedBy = "address")
    // @JsonIgnore
    // private List<Event> events;

    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private LocalDateTime creationTime;
    private boolean isDeleted;
}