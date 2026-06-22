package me.amjath.expense_tracker_api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.amjath.expense_tracker_api.user.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
