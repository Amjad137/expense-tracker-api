package me.amjath.expense_tracker_api.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {

    private int year;
    private int month;
    private String monthName;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;

    /** Breakdown by category for this month. */
    private List<CategoryReportResponse> expensesByCategory;
    private List<CategoryReportResponse> incomesByCategory;
}
