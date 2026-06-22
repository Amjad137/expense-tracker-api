package me.amjath.expense_tracker_api.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.amjath.expense_tracker_api.budget.dto.BudgetResponse;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.income.dto.IncomeResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Current month summary
    private int year;
    private int month;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    // Budget overview for current month
    private List<BudgetResponse> budgetSummary;

    // Recent transactions
    private List<ExpenseResponse> recentExpenses;
    private List<IncomeResponse> recentIncomes;
}
