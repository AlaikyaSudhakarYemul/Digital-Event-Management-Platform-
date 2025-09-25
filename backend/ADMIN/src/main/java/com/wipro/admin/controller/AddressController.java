package com.wipro.admin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.wipro.admin.entity.Address;
import com.wipro.admin.service.AddressService;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="http://localhost:3000")
public class AddressController {
 
    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;
 
    public AddressController(AddressService addressService){
        logger.info("Initializing AddressController with AddressService");
        this.addressService = addressService;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        logger.info("Creating address: {}", address);
        if(address == null){
            logger.error("Invalid address data: {}", address);
            return ResponseEntity.badRequest().body("Invalid request body.");
        }  
        Address addedAddress = addressService.addAddress(address);    
        logger.info("Address created successfully: {}", addedAddress);
        return new ResponseEntity<>(addedAddress, HttpStatus.CREATED);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable int id, @RequestBody Address address) {
        logger.info("Updating address with ID: {}", id);
        if(address == null || id < 0){
            logger.error("Invalid address data for ID {}: {}", id, address);
            return ResponseEntity.badRequest().body("Invalid request body.");
        }
        Address updatedAddress = addressService.updateAddress(id, address);
        logger.info("Address updated successfully: {}", updatedAddress);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable int id) {
        logger.info("Fetching address with ID: {}", id);
        String token = null;
        try {
            token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getHeader("Authorization");
        } catch (Exception e) {
            System.out.println("Could not fetch Authorization header: " + e.getMessage());
        }
        System.out.println("Authorization header received: " + token);
        if(id < 0){
            logger.error("Invalid address ID: {}", id);
            return ResponseEntity.badRequest().body("Invalid address ID.");
        }
        
        Address address = addressService.getAddress(id);
        if (address == null) {
            logger.warn("Address with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Address with ID {} fetched successfully: {}", id, address);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Address>> getAllAddresses() {
        logger.info("Fetching all addresses");
        List<Address> addresses = addressService.getAllAddresses();
        logger.info("All addresses fetched successfully, count: {}", addresses.size());
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }
 
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id){
        logger.info("Deleting address with ID: {}", id);
        if(id < 0){
            logger.error("Invalid address ID for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid address ID.");
        }
        addressService.deleteAddress(id);
        logger.info("Address with ID {} deleted successfully", id);
        return ResponseEntity.ok("Address deleted successfully!");
    }
}