package com.wipro.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wipro.user.entity.Users;
import com.wipro.user.exception.UserNotFoundException;
import com.wipro.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            .orElseThrow(() -> new UserNotFoundException("User not found!"));
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

        if (!existingUser.getUserName().equals(loggedInUsername)
            && !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new SecurityException("Unauthorized access - Insufficient permissions.");
        }

        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        existingUser.setUpdatedOn(LocalDate.now());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(int id) {
        Users user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setDeletedOn(LocalDate.now());
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public Users findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Users findById(int id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public Users updateContactNo(int id, String contactNo, String requesterEmail) {
        Users existingUser = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (requesterEmail == null || !requesterEmail.equalsIgnoreCase(existingUser.getEmail())) {
            throw new SecurityException("Unauthorized access - You can only update your own profile.");
        }

        existingUser.setContactNo(contactNo);
        existingUser.setUpdatedOn(LocalDate.now());
        return userRepository.save(existingUser);
    }

    @Override
    public void changePassword(int id, String currentPassword, String newPassword, String requesterEmail) {
        Users existingUser = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (requesterEmail == null || !requesterEmail.equalsIgnoreCase(existingUser.getEmail())) {
            throw new SecurityException("Unauthorized access - You can only change your own password.");
        }

        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setUpdatedOn(LocalDate.now());
        userRepository.save(existingUser);
    }
}
