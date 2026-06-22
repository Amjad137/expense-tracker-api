package me.amjath.expense_tracker_api.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.budget.dto.BudgetResponse;
import me.amjath.expense_tracker_api.budget.service.BudgetService;
import me.amjath.expense_tracker_api.dashboard.dto.DashboardResponse;
import me.amjath.expense_tracker_api.expense.mapper.ExpenseMapper;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.income.mapper.IncomeMapper;
import me.amjath.expense_tracker_api.income.repository.IncomeRepository;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetService budgetService;
    private final ExpenseMapper expenseMapper;
    private final IncomeMapper incomeMapper;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        BigDecimal totalIncome = incomeRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        BigDecimal totalExpense = expenseRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<BudgetResponse> budgetSummary = budgetService.getBudgetsForMonth(user, year, month);

        var recentExpenses = expenseRepository.findAllByUser(user,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "expenseDate")))
                .stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());

        var recentIncomes = incomeRepository.findTop5ByUserOrderByIncomeDateDesc(user)
                .stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Dashboard loaded for user: {} ({}/{})", user.getEmail(), year, month);

        return DashboardResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .budgetSummary(budgetSummary)
                .recentExpenses(recentExpenses)
                .recentIncomes(recentIncomes)
                .build();
    }
}
