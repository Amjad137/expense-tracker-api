package me.amjath.expense_tracker_api.budget.enums;

/**
 * Budget spending status.
 * OK       — spending < 80% of limit
 * WARNING  — spending >= 80% of limit
 * EXCEEDED — spending >= 100% of limit
 */
public enum BudgetStatus {
    OK,
    WARNING,
    EXCEEDED
}
