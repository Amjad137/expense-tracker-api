package me.amjath.expense_tracker_api.income.mapper;

import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.income.dto.IncomeResponse;
import me.amjath.expense_tracker_api.income.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    public IncomeResponse toResponse(Income income) {
        if (income == null) return null;

        Category category = income.getCategory();

        return IncomeResponse.builder()
                .id(income.getId())
                .title(income.getTitle())
                .amount(income.getAmount())
                .description(income.getDescription())
                .incomeDate(income.getIncomeDate())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }
}
