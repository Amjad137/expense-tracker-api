package me.amjath.expense_tracker_api.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.income.repository.IncomeRepository;
import me.amjath.expense_tracker_api.report.dto.CategoryReportResponse;
import me.amjath.expense_tracker_api.report.dto.MonthlyReportResponse;
import me.amjath.expense_tracker_api.report.projection.CategoryAmountSummary;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalIncome = incomeRepository.sumAmountByUserAndDateRange(user, startDate, endDate);
        BigDecimal totalExpense = expenseRepository.sumAmountByUserAndDateRange(user, startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<CategoryReportResponse> expensesByCategory = getCategoryBreakdown(user, startDate, endDate, totalExpense, false);
        List<CategoryReportResponse> incomesByCategory = getCategoryBreakdown(user, startDate, endDate, totalIncome, true);

        log.debug("Monthly report generated for user: {} ({}/{})", user.getEmail(), year, month);

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .monthName(Month.of(month).name())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .expensesByCategory(expensesByCategory)
                .incomesByCategory(incomesByCategory)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CategoryReportResponse> getCategoryReport(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        BigDecimal totalExpense = expenseRepository.sumAmountByUserAndDateRange(user, startDate, endDate);
        return getCategoryBreakdown(user, startDate, endDate, totalExpense, false);
    }

    // Private helpers
    private List<CategoryReportResponse> getCategoryBreakdown(
            User user, LocalDate startDate, LocalDate endDate,
            BigDecimal grandTotal, boolean isIncome) {

        // Use JPQL aggregation for category breakdown
        if (isIncome) {
            return getIncomeCategoryBreakdown(user, startDate, endDate, grandTotal);
        } else {
            return getExpenseCategoryBreakdown(user, startDate, endDate, grandTotal);
        }
    }

    private List<CategoryReportResponse> getExpenseCategoryBreakdown(
            User user, LocalDate startDate, LocalDate endDate, BigDecimal grandTotal) {
        return expenseRepository.getExpenseCategorySummaryByUserAndDateRange(user, startDate, endDate)
                .stream()
                .map(summary -> toCategoryReportResponse(summary, grandTotal))
                .toList();
    }

    private List<CategoryReportResponse> getIncomeCategoryBreakdown(
            User user, LocalDate startDate, LocalDate endDate, BigDecimal grandTotal) {
        return incomeRepository
                .getIncomeCategorySummaryByUserAndDateRange(user, startDate, endDate)
                .stream()
                .map(summary -> toCategoryReportResponse(summary, grandTotal))
                .toList();
    }

    private CategoryReportResponse toCategoryReportResponse(
            CategoryAmountSummary summary,
            BigDecimal grandTotal
    ) {
        double percentageOfTotal = grandTotal.compareTo(BigDecimal.ZERO) > 0
                ? summary.getTotalAmount()
                  .multiply(BigDecimal.valueOf(100))
                  .divide(grandTotal, 2, RoundingMode.HALF_UP)
                  .doubleValue()
                : 0.0;

        return CategoryReportResponse.builder()
                .categoryId(summary.getCategoryId())
                .categoryName(summary.getCategoryName())
                .categoryIcon(summary.getCategoryIcon())
                .categoryColor(summary.getCategoryColor())
                .totalAmount(summary.getTotalAmount())
                .transactionCount(summary.getTransactionCount())
                .percentageOfTotal(percentageOfTotal)
                .build();
    }
}
