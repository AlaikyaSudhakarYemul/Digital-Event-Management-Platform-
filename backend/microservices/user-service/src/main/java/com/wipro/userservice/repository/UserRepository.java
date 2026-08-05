package com.wipro.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.userservice.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {

    boolean existsById(int id);

    Optional<Users> findByUserName(String username);

    Optional<Users> findByEmail(String email);
}
