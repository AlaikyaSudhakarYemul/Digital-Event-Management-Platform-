package com.wipro.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.eventservice.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
}
