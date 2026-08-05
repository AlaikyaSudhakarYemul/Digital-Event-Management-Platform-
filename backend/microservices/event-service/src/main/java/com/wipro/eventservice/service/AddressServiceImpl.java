package com.wipro.eventservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wipro.eventservice.entity.Address;
import com.wipro.eventservice.exception.AddressNotFoundException;
import com.wipro.eventservice.repository.AddressRepository;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address addAddress(Address address) {
        address.setCreatedOn(LocalDate.now());
        address.setCreationTime(LocalDateTime.now());
        address.setUpdatedOn(LocalDate.now());
        address.setDeleted(false);
        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(int id, Address address) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));
        existing.setAddress(address.getAddress());
        existing.setCity(address.getCity());
        existing.setState(address.getState());
        existing.setCountry(address.getCountry());
        existing.setPincode(address.getPincode());
        existing.setUpdatedOn(LocalDate.now());
        return addressRepository.save(existing);
    }

    @Override
    public void deleteAddress(int id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));
        address.setDeletedOn(LocalDate.now());
        address.setDeleted(true);
        addressRepository.save(address);
    }

    @Override
    public Address getAddress(int id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));
    }

    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }
}
