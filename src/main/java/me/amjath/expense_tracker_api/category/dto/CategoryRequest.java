package me.amjath.expense_tracker_api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String icon;

    @Size(max = 20, message = "Color code must not exceed 20 characters")
    private String color;
}
