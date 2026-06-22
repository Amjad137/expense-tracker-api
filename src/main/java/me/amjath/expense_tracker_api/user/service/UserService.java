package me.amjath.expense_tracker_api.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.user.dto.ChangePasswordRequest;
import me.amjath.expense_tracker_api.user.dto.UpdateProfileRequest;
import me.amjath.expense_tracker_api.user.dto.UserResponse;
import me.amjath.expense_tracker_api.user.entity.User;
import me.amjath.expense_tracker_api.user.mapper.UserMapper;
import me.amjath.expense_tracker_api.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** Get the currently authenticated user's profile. */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User currentUser) {
        return userMapper.toResponse(currentUser);
    }

    /** Get a user by ID (ADMIN only). */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    /** List all users with pagination (ADMIN only). */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PagedResponse.of(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    /** Update profile (name only). */
    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        User updated = userRepository.save(currentUser);
        log.info("Profile updated for user: {}", currentUser.getEmail());
        return userMapper.toResponse(updated);
    }

    /** Change password with current-password verification. */
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        log.info("Password changed for user: {}", currentUser.getEmail());
    }

    /** Soft-delete a user account (ADMIN only). */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        user.softDelete();
        userRepository.save(user);
        log.info("User soft-deleted: {}", user.getEmail());
    }
}
