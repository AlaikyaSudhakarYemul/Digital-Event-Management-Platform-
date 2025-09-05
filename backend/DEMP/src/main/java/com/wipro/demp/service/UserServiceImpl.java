package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.wipro.demp.entity.Users;
import com.wipro.demp.exception.UserNotFoundException;
import com.wipro.demp.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
 
   @Autowired
    private PasswordEncoder passwordEncoder;
 
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
 
    @Override
    public Users registerUser(Users user) {
        userRepository.findByUserName(user.getUserName()).ifPresent(existingUser -> {
            throw new IllegalArgumentException("User already exists!");
        });
 
        user.setCreatedOn(LocalDate.now());
        user.setCreationTime(LocalDateTime.now());
        user.setUpdatedOn(LocalDate.now());
        user.setDeleted(false);
 
        return userRepository.save(user);
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
   
    @Override
    public Users findByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found!");
                });
    }
 
    @Override
    public Users updateUser(int id, Users updatedUser) {
       
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Unauthorized access - User not authenticated.");
        }
 
        String loggedInUsername = authentication.getName();
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
 
        if (!existingUser.getUserName().equals(loggedInUsername) && !authentication.getAuthorities().contains(new SimpleGrantedAuthority("USER"))) {
            throw new SecurityException("Unauthorized access - Insufficient permissions.");
        }
 
        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        existingUser.setUpdatedOn(LocalDate.now());
 
        return userRepository.save(existingUser);
    }
 
    @Override
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        Optional<Users> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            user.setDeletedOn(LocalDate.now());
            user.setDeleted(true);
            userRepository.save(user);
        }
    }
 
    @Override
    public Users findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
 
    
}
