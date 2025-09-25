package com.wipro.admin.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.util.List;

// import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int addressId;

    @Size(min=5, max=50, message = "Address must be more than 5 and less than or equal to 50 characters")
    private String address;

    private String state;
    private String country;

    @NotNull
    @Pattern(regexp = "\\d{6}", message = "Pincode must be exactly 6 digits")
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