package me.amjath.expense_tracker_api.budget.mapper;

import me.amjath.expense_tracker_api.budget.dto.BudgetResponse;
import me.amjath.expense_tracker_api.budget.entity.Budget;
import me.amjath.expense_tracker_api.budget.enums.BudgetStatus;
import me.amjath.expense_tracker_api.category.entity.Category;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        if (budget == null) return null;

        Category category = budget.getCategory();
        BigDecimal limit = budget.getLimitAmount();
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        BigDecimal remaining = limit.subtract(spent);

        double percentage = 0.0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.multiply(BigDecimal.valueOf(100))
                    .divide(limit, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        BudgetStatus status = calculateStatus(limit, spent);

        return BudgetResponse.builder()
                .id(budget.getId())
                .budgetType(budget.getBudgetType())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .categoryColor(category != null ? category.getColor() : null)
                .limitAmount(limit)
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageUsed(percentage)
                .status(status)
                .year(budget.getYear())
                .month(budget.getMonth())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }

    private BudgetStatus calculateStatus(BigDecimal limit, BigDecimal spent) {
        if (spent.compareTo(limit) >= 0) {
            return BudgetStatus.EXCEEDED;
        }
        BigDecimal percentage = spent.multiply(BigDecimal.valueOf(100))
                .divide(limit, 0, RoundingMode.HALF_UP);
        if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BudgetStatus.WARNING;
        }
        return BudgetStatus.OK;
    }
}
