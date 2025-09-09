package com.wipro.demp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wipro.demp.entity.Users;
 
@Service
public interface UserService {
   
    public Users registerUser(Users user);
   
    public List<Users> getAllUsers();
   
    public Users findByUsername(String username);
   
    public Users updateUser(int id, Users updatedUser);
 
    public Users findByEmail(String email);
 
    public void deleteUser(int id);
 
}