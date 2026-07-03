package com.wipro.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.user.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByUserName(String username);

    Optional<Users> findByEmail(String email);
}
