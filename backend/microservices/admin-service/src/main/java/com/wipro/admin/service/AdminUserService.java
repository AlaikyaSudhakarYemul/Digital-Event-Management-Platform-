package com.wipro.admin.service;

import java.util.List;

import com.wipro.admin.entity.Users;

public interface AdminUserService {

    List<Users> getAllUsers();

    List<Users> getOrganizers();
}
