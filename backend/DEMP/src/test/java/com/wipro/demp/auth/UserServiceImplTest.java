package com.wipro.demp.auth;


import com.wipro.demp.entity.Users;
import com.wipro.demp.repository.UserRepository;
import com.wipro.demp.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testRegisterUserSuccess() {
        Users newUser = new Users();
        newUser.setUserName("johndoe");

        when(userRepository.findByUserName("johndoe")).thenReturn(Optional.empty());
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users saved = invocation.getArgument(0);
            saved.setUserId(1); // simulate generated ID
            return saved;
        });

        Users result = userService.registerUser(newUser);

        assertEquals("johndoe", result.getUserName());
        assertEquals(1, result.getUserId());
        assertNotNull(result.getCreatedOn());
        assertNotNull(result.getUpdatedOn());
        verify(userRepository).save(any(Users.class));
    }

    @Test
    void testRegisterUserUserAlreadyExists() {
        Users newUser = new Users();
        newUser.setUserName("johndoe");

        when(userRepository.findByUserName("johndoe"))
                .thenReturn(Optional.of(new Users()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(newUser);
        });

        assertEquals("User already exists!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindByEmailUserExists() {
        Users mockUser = new Users();
        mockUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(mockUser));

        Users result = userService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmailUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

        Users result = userService.findByEmail("missing@example.com");

        assertNull(result);
        verify(userRepository).findByEmail("missing@example.com");
    }

}
