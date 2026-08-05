package com.wipro.eventservice.service;

import java.util.List;

import com.wipro.eventservice.entity.Address;

public interface AddressService {
    Address addAddress(Address address);
    Address updateAddress(int id, Address address);
    void deleteAddress(int id);
    Address getAddress(int id);
    List<Address> getAllAddresses();
}
