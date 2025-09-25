package com.wipro.demp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private List<Event> events;

    private LocalDate createdOn;
    private LocalDate updatedOn;
    private LocalDate deletedOn;
    private LocalDateTime creationTime;
    private boolean isDeleted;

}