package com.wipro.demp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.demp.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {
 
    boolean existsById(int id);
   
    Optional<Users> findByUserName(String username);
   
    Optional<Users> findByEmail(String email);
 
}
