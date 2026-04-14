package com.wipro.demp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import com.wipro.demp.entity.Users;
import com.wipro.demp.exception.UserNotFoundException;
import com.wipro.demp.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Users registerUser(Users user) {
        userRepository.findByUserName(user.getUserName()).ifPresent(existingUser -> {
            throw new IllegalArgumentException("User already exists!");
        });

        user.setCreatedOn(LocalDate.now());
        user.setCreationTime(LocalDateTime.now());
        user.setUpdatedOn(LocalDate.now());
        user.setDeleted(false);

        Users savedUser = userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(savedUser.getEmail());
        message.setSubject("EVENTRA Account Creation Confirmation");
        String msg="Dear "+savedUser.getUserName()+",\n\nWelcome to *EVENTRA* Platform. This is a Confirmation mail for your successful registration on this website.\n"+
        "We’re excited to have you on board and hope you enjoy exploring everything EVENTRA has to offer.\n\n"
        		+ "*What you can do next:*\n" +
                "- Explore upcoming events\n" +
                "- Customize your profile\n" +
                "- Connect with other attendees\n\n" +
                "If you have any questions or need help, feel free to reach out to our support team.\n\n" +
                "Thank you and welcome again!\n\n" +
                "Best regards,\n" +
                "The EVENTRA Team";
        message.setText(msg);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            logger.error("Registration mail failed for user {} ({}): {}", savedUser.getUserName(), savedUser.getEmail(), ex.getMessage());
            throw new IllegalStateException("Registration failed because confirmation email could not be sent. Please try again.");
        }

        return savedUser;
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

        if (!existingUser.getUserName().equals(loggedInUsername)
                && !authentication.getAuthorities().contains(new SimpleGrantedAuthority("USER"))) {
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

        Optional<Users> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {

            Users user = optionalUser.get();
            if (user.isDeleted()) {
                return null;
            }
            return optionalUser.get();
        } else {
            return null;
        }

        // return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Users getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
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
