package com.wipro.demp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.wipro.demp.entity.Address;
 
public interface AddressRepository extends JpaRepository<Address,Integer> {
    
}