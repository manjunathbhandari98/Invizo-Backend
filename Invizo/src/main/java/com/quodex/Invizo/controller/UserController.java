package com.quodex.Invizo.controller;

import com.quodex.Invizo.io.UserRequest;
import com.quodex.Invizo.io.UserResponse;
import com.quodex.Invizo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserController  {
    private final UserService userService;

    @PostMapping("/register")
    public UserResponse registerUser(@RequestBody UserRequest user){
        try{
            return userService.createUser(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create user");
        }
    }

    @GetMapping("/users")
    public List<UserResponse> fetchUsers(){
        return userService.fetchUsers();
    }

    @DeleteMapping("/user/{userId}")
    public void deleteUser(@PathVariable String userId){
        try{
            userService.deleteUsers(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
