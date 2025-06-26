package com.quodex.Invizo.service;

import com.quodex.Invizo.io.UserRequest;
import com.quodex.Invizo.io.UserResponse;
import com.quodex.Invizo.util.Role;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    Role getUserRole(String email);
    List<UserResponse> fetchUsers();
    void deleteUsers(String userId);
}
