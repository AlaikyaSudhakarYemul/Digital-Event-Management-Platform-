package com.wipro.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wipro.admin.entity.Role;
import com.wipro.admin.entity.Users;
import com.wipro.admin.repository.UserRepository;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<Users> getOrganizers() {
        return userRepository.findByRole(Role.ORGANIZER);
    }
}
