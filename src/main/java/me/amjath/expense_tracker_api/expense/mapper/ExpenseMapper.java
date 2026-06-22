package me.amjath.expense_tracker_api.expense.mapper;

import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.expense.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) return null;

        Category category = expense.getCategory();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .createdBy(expense.getCreatedBy())
                .build();
    }
}
