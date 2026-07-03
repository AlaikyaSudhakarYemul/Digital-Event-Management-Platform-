package com.wipro.user.service;

import java.util.List;

import com.wipro.user.entity.Users;

public interface UserService {

    Users registerUser(Users user);

    List<Users> getAllUsers();

    Users findByUsername(String username);

    Users updateUser(int id, Users updatedUser);

    Users findByEmail(String email);

    Users findById(int id);

    Users updateContactNo(int id, String contactNo, String requesterEmail);

    void changePassword(int id, String currentPassword, String newPassword, String requesterEmail);

    void deleteUser(int id);
}
