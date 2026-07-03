package com.wipro.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.admin.entity.Role;
import com.wipro.admin.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {

    List<Users> findByRole(Role role);
}
