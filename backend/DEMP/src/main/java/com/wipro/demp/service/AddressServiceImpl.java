package com.wipro.demp.service;

 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
import org.springframework.stereotype.Service;
 
import com.wipro.demp.entity.Address;
import com.wipro.demp.repository.AddressRepository;
import com.wipro.demp.exception.AddressNotFoundException;
 
@Service
public class AddressServiceImpl implements AddressService {
 
    private final AddressRepository addressRepository;
 
    public AddressServiceImpl(AddressRepository addressRepository){
        this.addressRepository = addressRepository;
    }
 
    @Override
    public Address addAddress(Address address) {
        address.setCreatedOn(LocalDate.now());
        address.setCreationTime(LocalDateTime.now());
        address.setUpdatedOn(LocalDate.now());
        address.setDeleted(false);
        Address newAddress = addressRepository.save(address);
        return newAddress;
    }
 
    @Override
    public Address updateAddress(int id, Address address) {
        Address existingAddress = addressRepository.findById(id)
                                    .orElseThrow(() -> new AddressNotFoundException("Address not found with id: "+id));
 
        existingAddress.setAddress(address.getAddress());
        existingAddress.setState(address.getState());
        existingAddress.setCountry(address.getCountry());
        existingAddress.setPincode(address.getPincode());
        existingAddress.setUpdatedOn(LocalDate.now());
        
        return addressRepository.save(existingAddress);
    }
 
    @Override
    public void deleteAddress(int id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException("Address not found with id: " + id);
        }
        Optional<Address> addressOpt = addressRepository.findById(id);
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            address.setDeletedOn(LocalDate.now());
            address.setDeleted(true);
            addressRepository.save(address);
        }
        // addressRepository.deleteById(id);
    }
 
    @Override
    public Address getAddress(int id) {
        
        return addressRepository.findById(id)
                                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: "+id));
    }
 
    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }
    
}