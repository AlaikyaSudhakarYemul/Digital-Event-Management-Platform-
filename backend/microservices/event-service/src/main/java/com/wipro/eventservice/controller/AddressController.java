package com.wipro.eventservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wipro.eventservice.entity.Address;
import com.wipro.eventservice.service.AddressService;

@RestController
@RequestMapping("/api/admin")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        if (address == null) return ResponseEntity.badRequest().body("Invalid request body.");
        return new ResponseEntity<>(addressService.addAddress(address), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable int id, @RequestBody Address address) {
        if (address == null || id < 0) return ResponseEntity.badRequest().body("Invalid request body.");
        return new ResponseEntity<>(addressService.updateAddress(id, address), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable int id) {
        if (id < 0) return ResponseEntity.badRequest().body("Invalid address ID.");
        Address address = addressService.getAddress(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Address>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
