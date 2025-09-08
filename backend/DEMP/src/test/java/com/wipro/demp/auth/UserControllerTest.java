package com.wipro.demp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wipro.demp.config.JwtUtil;
import com.wipro.demp.controller.UserController;
import com.wipro.demp.entity.Role;
import com.wipro.demp.entity.Users;
import com.wipro.demp.exception.GlobalExceptionHandler;
import com.wipro.demp.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({ GlobalExceptionHandler.class, TestConfig.class })
@WebMvcTest(UserController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUserName("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
    }

    @Test
    void testRegisterSuccessful() throws Exception {
        Users userToRegister = new Users();
        userToRegister.setUserName("testuser");
        userToRegister.setEmail("testuser@example.com");
        userToRegister.setPassword("Test@1234");

        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        Mockito.when(userService.findByEmail("testuser@example.com")).thenReturn(null);
        Mockito.when(userService.registerUser(any(Users.class))).thenReturn(testUser);
        Mockito.when(jwtUtil.generateToken(eq(testUser.getEmail()), eq(testUser.getRole())))
                .thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userToRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.userName").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void testRegisterUserAlreadyExists() throws Exception {
        Users userToRegister = new Users();
        userToRegister.setUserName("testuser");
        userToRegister.setEmail("testuser@example.com");
        userToRegister.setPassword("Test@1234");

        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        Mockito.when(userService.findByEmail("testuser@example.com")).thenReturn(new Users());
        Mockito.when(userService.registerUser(any(Users.class)))
                .thenThrow(new IllegalArgumentException("User already exists"));

        Map<String, String> userData = Map.of(
                "userName", "testuser",
                "email", "testuser@example.com",
                "password", "Test@1234");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void testLoginSuccessful() throws Exception {
        Mockito.when(userService.findByEmail("testuser@example.com")).thenReturn(testUser);
        Mockito.when(passwordEncoder.matches("plaintext", "encodedPassword")).thenReturn(true);
        Mockito.when(jwtUtil.generateToken(eq(testUser.getEmail()), eq(testUser.getRole())))
                .thenReturn("mocked-jwt-token");

        Map<String, String> loginData = Map.of(
                "email", "testuser@example.com",
                "password", "plaintext");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        Mockito.when(userService.findByEmail("testuser@example.com")).thenReturn(testUser);
        Mockito.when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        Map<String, String> loginData = Map.of(
                "email", "testuser@example.com",
                "password", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginData)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

}