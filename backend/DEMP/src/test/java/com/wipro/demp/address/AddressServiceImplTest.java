package com.wipro.demp.address;

import com.wipro.demp.entity.Address;
import com.wipro.demp.exception.AddressNotFoundException;
import com.wipro.demp.repository.AddressRepository;
import com.wipro.demp.service.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddressServiceImplTest {

    private AddressRepository addressRepository;
    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        addressRepository = Mockito.mock(AddressRepository.class);
        addressService = new AddressServiceImpl(addressRepository);
    }

    private Address sampleAddress() {
        Address address = new Address();
        address.setAddressId(1);
        address.setAddress("Madhapur Street 1");
        address.setState("TS");
        address.setCountry("India");
        address.setPincode("500081");
        return address;
    }

    @Test
    void addAddressSetsAuditFields() {
        Address address = sampleAddress();
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address saved = addressService.addAddress(address);

        assertNotNull(saved.getCreatedOn());
        assertNotNull(saved.getUpdatedOn());
        assertNotNull(saved.getCreationTime());
        assertTrue(!saved.isDeleted());
    }

    @Test
    void updateAddressSuccess() {
        Address existing = sampleAddress();
        Address update = sampleAddress();
        update.setAddress("Gachibowli Main Road");

        when(addressRepository.findById(1)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address result = addressService.updateAddress(1, update);

        assertEquals("Gachibowli Main Road", result.getAddress());
        assertNotNull(result.getUpdatedOn());
    }

    @Test
    void updateAddressMissingThrows() {
        when(addressRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.updateAddress(7, sampleAddress()));
    }

    @Test
    void deleteAddressMarksDeleted() {
        Address existing = sampleAddress();
        when(addressRepository.existsById(1)).thenReturn(true);
        when(addressRepository.findById(1)).thenReturn(Optional.of(existing));

        addressService.deleteAddress(1);

        assertTrue(existing.isDeleted());
        assertNotNull(existing.getDeletedOn());
        verify(addressRepository).save(existing);
    }

    @Test
    void deleteAddressMissingThrows() {
        when(addressRepository.existsById(42)).thenReturn(false);

        assertThrows(AddressNotFoundException.class, () -> addressService.deleteAddress(42));
    }

    @Test
    void getAddressAndGetAllAddresses() {
        Address existing = sampleAddress();
        when(addressRepository.findById(1)).thenReturn(Optional.of(existing));
        when(addressRepository.findAll()).thenReturn(List.of(existing));

        Address result = addressService.getAddress(1);
        List<Address> all = addressService.getAllAddresses();

        assertEquals(1, result.getAddressId());
        assertEquals(1, all.size());
    }
}
