package com.wipro.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wipro.admin.entity.Address;
import com.wipro.admin.exception.AddressNotFoundException;
import com.wipro.admin.repository.AddressRepository;

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
        Address existingAddress = addressRepository.findById(id)
            .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));

        existingAddress.setAddress(address.getAddress());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());
        existingAddress.setCountry(address.getCountry());
        existingAddress.setPincode(address.getPincode());
        existingAddress.setUpdatedOn(LocalDate.now());

        return addressRepository.save(existingAddress);
    }

    @Override
    public void deleteAddress(int id) {
        Address existingAddress = addressRepository.findById(id)
            .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));

        existingAddress.setDeletedOn(LocalDate.now());
        existingAddress.setDeleted(true);
        addressRepository.save(existingAddress);
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
