package me.amjath.expense_tracker_api.recurring.mapper;

import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.recurring.dto.RecurringExpenseResponse;
import me.amjath.expense_tracker_api.recurring.entity.RecurringExpense;
import org.springframework.stereotype.Component;

@Component
public class RecurringExpenseMapper {

    public RecurringExpenseResponse toResponse(RecurringExpense recurring) {
        if (recurring == null) return null;

        Category category = recurring.getCategory();

        return RecurringExpenseResponse.builder()
                .id(recurring.getId())
                .title(recurring.getTitle())
                .amount(recurring.getAmount())
                .description(recurring.getDescription())
                .frequency(recurring.getFrequency())
                .startDate(recurring.getStartDate())
                .nextDueDate(recurring.getNextDueDate())
                .endDate(recurring.getEndDate())
                .active(recurring.isActive())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .createdAt(recurring.getCreatedAt())
                .updatedAt(recurring.getUpdatedAt())
                .build();
    }
}
