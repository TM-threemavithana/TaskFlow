package com.taskflow.service;

import com.taskflow.dto.DtoMapper;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User service. Separate from AuthService to follow the Single Responsibility Principle:
 * AuthService handles authentication; this service handles user management.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return DtoMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(DtoMapper::toUserResponse)    // METHOD REFERENCE: same as u -> DtoMapper.toUserResponse(u)
                .toList();                          // STREAM: transforms a list of Users into a list of UserResponses
    }
}
