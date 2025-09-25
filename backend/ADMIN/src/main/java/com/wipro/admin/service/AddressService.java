package com.wipro.admin.service;

import java.util.List;

import com.wipro.admin.entity.Address;

public interface AddressService {
 
    public Address addAddress(Address address);
 
    public Address updateAddress(int id, Address address);
 
    public void deleteAddress(int id);
 
    public Address getAddress(int id);
 
    public List<Address> getAllAddresses();
    
}
 