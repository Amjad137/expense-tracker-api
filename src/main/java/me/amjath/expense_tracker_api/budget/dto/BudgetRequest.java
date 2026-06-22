package me.amjath.expense_tracker_api.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BudgetRequest {

    @NotNull(message = "Budget type is required")
    private BudgetType budgetType;

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
    private BigDecimal limitAmount;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    /** Required when budgetType is CATEGORY. */
    private UUID categoryId;
}
