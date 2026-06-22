package me.amjath.expense_tracker_api.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.income.repository.IncomeRepository;
import me.amjath.expense_tracker_api.report.dto.CategoryReportResponse;
import me.amjath.expense_tracker_api.report.dto.MonthlyReportResponse;
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

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

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

        // Fetch all expenses for the period (paginated for safety)
        var expenses = expenseRepository.findAllByUser(user,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("expenseDate")));

        // Group by category, filter by date
        var grouped = expenses.getContent().stream()
                .filter(e -> !e.getExpenseDate().isBefore(startDate) && !e.getExpenseDate().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory().getName() : "Uncategorized",
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    var categoryExpenses = entry.getValue();
                    var cat = categoryExpenses.get(0).getCategory();
                    BigDecimal catTotal = categoryExpenses.stream()
                            .map(me.amjath.expense_tracker_api.expense.entity.Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? catTotal.multiply(BigDecimal.valueOf(100))
                              .divide(grandTotal, 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;

                    return CategoryReportResponse.builder()
                            .categoryId(cat != null ? cat.getId() : null)
                            .categoryName(entry.getKey())
                            .categoryIcon(cat != null ? cat.getIcon() : null)
                            .categoryColor(cat != null ? cat.getColor() : null)
                            .totalAmount(catTotal)
                            .transactionCount(categoryExpenses.size())
                            .percentageOfTotal(pct)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    private List<CategoryReportResponse> getIncomeCategoryBreakdown(
            User user, LocalDate startDate, LocalDate endDate, BigDecimal grandTotal) {

        var incomes = incomeRepository.findAllByUser(user,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("incomeDate")));

        var grouped = incomes.getContent().stream()
                .filter(i -> !i.getIncomeDate().isBefore(startDate) && !i.getIncomeDate().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        i -> i.getCategory() != null ? i.getCategory().getName() : "Uncategorized",
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    var categoryIncomes = entry.getValue();
                    var cat = categoryIncomes.get(0).getCategory();
                    BigDecimal catTotal = categoryIncomes.stream()
                            .map(me.amjath.expense_tracker_api.income.entity.Income::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? catTotal.multiply(BigDecimal.valueOf(100))
                              .divide(grandTotal, 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;

                    return CategoryReportResponse.builder()
                            .categoryId(cat != null ? cat.getId() : null)
                            .categoryName(entry.getKey())
                            .categoryIcon(cat != null ? cat.getIcon() : null)
                            .categoryColor(cat != null ? cat.getColor() : null)
                            .totalAmount(catTotal)
                            .transactionCount(categoryIncomes.size())
                            .percentageOfTotal(pct)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }
}
