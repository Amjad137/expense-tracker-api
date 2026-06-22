package me.amjath.expense_tracker_api.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private boolean system;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
