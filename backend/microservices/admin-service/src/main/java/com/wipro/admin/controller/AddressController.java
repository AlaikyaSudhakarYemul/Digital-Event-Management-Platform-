package com.wipro.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.admin.constants.AdminServiceConstants;
import com.wipro.admin.entity.Address;
import com.wipro.admin.service.AddressService;

@RestController
@RequestMapping(AdminServiceConstants.API_URL + AdminServiceConstants.ADMIN_URL)
@CrossOrigin(origins = AdminServiceConstants.FRONTEND_URL)
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        if (address == null) {
            return ResponseEntity.badRequest().body("Invalid request body.");
        }

        Address addedAddress = addressService.addAddress(address);
        return new ResponseEntity<>(addedAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable int id, @RequestBody Address address) {
        if (address == null || id < 0) {
            return ResponseEntity.badRequest().body("Invalid request body.");
        }

        Address updatedAddress = addressService.updateAddress(id, address);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().body("Invalid address ID.");
        }

        Address address = addressService.getAddress(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Address>> getAllAddresses() {
        return new ResponseEntity<>(addressService.getAllAddresses(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id) {
        if (id < 0) {
            return ResponseEntity.badRequest().body("Invalid address ID.");
        }

        addressService.deleteAddress(id);
        return ResponseEntity.ok("Address deleted successfully!");
    }
}
