package me.amjath.expense_tracker_api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.user.dto.ChangePasswordRequest;
import me.amjath.expense_tracker_api.user.dto.UpdateProfileRequest;
import me.amjath.expense_tracker_api.user.dto.UserResponse;
import me.amjath.expense_tracker_api.user.entity.User;
import me.amjath.expense_tracker_api.user.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "User profile and account management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current authenticated user's profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(currentUser)));
    }

    @Operation(summary = "Update current user's profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", userService.updateProfile(currentUser, request)));
    }

    @Operation(summary = "Change current user's password")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // ---------------------------------------------------------------
    // Admin-only endpoints
    // ---------------------------------------------------------------

    @Operation(summary = "Get all users (ADMIN only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    @Operation(summary = "Get user by ID (ADMIN only)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @Operation(summary = "Soft-delete a user (ADMIN only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
