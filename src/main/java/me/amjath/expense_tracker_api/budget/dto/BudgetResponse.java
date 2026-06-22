package me.amjath.expense_tracker_api.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.amjath.expense_tracker_api.budget.enums.BudgetStatus;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private UUID id;
    private BudgetType budgetType;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double percentageUsed;
    private BudgetStatus status;
    private int year;
    private int month;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
