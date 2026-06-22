package me.amjath.expense_tracker_api.category.mapper;

import me.amjath.expense_tracker_api.category.dto.CategoryResponse;
import me.amjath.expense_tracker_api.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .system(category.isSystem())
                .userId(category.getUser() != null ? category.getUser().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
