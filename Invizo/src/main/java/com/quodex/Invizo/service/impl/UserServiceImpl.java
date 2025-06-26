package com.quodex.Invizo.service.impl;

import com.quodex.Invizo.entity.UserEntity;
import com.quodex.Invizo.io.UserRequest;
import com.quodex.Invizo.io.UserResponse;
import com.quodex.Invizo.repository.UserRepository;
import com.quodex.Invizo.service.UserService;
import com.quodex.Invizo.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequest request) {
        UserEntity user = convertToEntity(request);
        user = userRepository.save(user);
        return convertToResponse(user);
    }


    @Override
    public Role getUserRole(String email) {
        UserEntity user =  userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found for Email "+email));
        return user.getRole();
    }

    @Override
    public List<UserResponse> fetchUsers() {
        return userRepository.findAll()
                .stream().map(user -> convertToResponse(user))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUsers(String userId) {
        UserEntity user = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        userRepository.delete(user);
    }

    private UserResponse convertToResponse(UserEntity user) {
        return UserResponse.builder()
                .name(user.getName())
                .userId(user.getUserId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole())
                .build();
    }

    private UserEntity convertToEntity(UserRequest request) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
    }
}
