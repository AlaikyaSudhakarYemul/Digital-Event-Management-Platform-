package com.wipro.userservice.service;

import java.util.List;

import com.wipro.userservice.entity.Users;

public interface UserService {

    Users registerUser(Users user);

    Users findByUsername(String username);

    Users findByEmail(String email);

    Users findById(int id);

    List<Users> getAllUsers();

    Users updateUser(int id, Users user);

    void deleteUser(int id);

    Users updateContactNo(int id, String contactNo, String requesterEmail);

    void changePassword(int id, String currentPassword, String newPassword, String requesterEmail);
}
