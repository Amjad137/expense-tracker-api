package me.amjath.expense_tracker_api.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.budget.dto.BudgetRequest;
import me.amjath.expense_tracker_api.budget.dto.BudgetResponse;
import me.amjath.expense_tracker_api.budget.entity.Budget;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;
import me.amjath.expense_tracker_api.budget.mapper.BudgetMapper;
import me.amjath.expense_tracker_api.budget.repository.BudgetRepository;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMapper budgetMapper;

    @Transactional(readOnly = true)
    public PagedResponse<BudgetResponse> getBudgets(User user, Pageable pageable) {
        return PagedResponse.of(
                budgetRepository.findAllByUser(user, pageable)
                        .map(budget -> {
                            BigDecimal spent = calculateSpent(budget, user);
                            return budgetMapper.toResponse(budget, spent);
                        })
        );
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(UUID id, User user) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Budget", id));
        BigDecimal spent = calculateSpent(budget, user);
        return budgetMapper.toResponse(budget, spent);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsForMonth(User user, int year, int month) {
        return budgetRepository.findAllByUserAndYearAndMonth(user, year, month)
                .stream()
                .map(budget -> {
                    BigDecimal spent = calculateSpent(budget, user);
                    return budgetMapper.toResponse(budget, spent);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request, User user) {
        validateBudgetRequest(request, user);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setBudgetType(request.getBudgetType());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setYear(request.getYear());
        budget.setMonth(request.getMonth());

        if (request.getBudgetType() == BudgetType.CATEGORY) {
            if (request.getCategoryId() == null) {
                throw new BadRequestException("Category ID is required for CATEGORY type budgets");
            }
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            budget.setCategory(category);
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created: {} ({}/{}) for user: {}", saved.getId(), request.getYear(), request.getMonth(), user.getEmail());
        return budgetMapper.toResponse(saved, calculateSpent(saved, user));
    }

    @Transactional
    public BudgetResponse updateBudget(UUID id, BudgetRequest request, User user) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Budget", id));

        budget.setLimitAmount(request.getLimitAmount());

        Budget updated = budgetRepository.save(budget);
        log.info("Budget updated: {}", id);
        return budgetMapper.toResponse(updated, calculateSpent(updated, user));
    }

    @Transactional
    public void deleteBudget(UUID id, User user) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Budget", id));
        budget.softDelete();
        budgetRepository.save(budget);
        log.info("Budget soft-deleted: {}", id);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private BigDecimal calculateSpent(Budget budget, User user) {
        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        if (budget.getBudgetType() == BudgetType.GLOBAL) {
            return expenseRepository.sumAmountByUserAndDateRange(user, startDate, endDate);
        } else {
            return expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                    user,
                    budget.getCategory().getId(),
                    startDate, endDate);
        }
    }

    private void validateBudgetRequest(BudgetRequest request, User user) {
        if (request.getBudgetType() == BudgetType.GLOBAL) {
            boolean exists = budgetRepository
                    .findByUserAndBudgetTypeAndYearAndMonthAndCategoryIsNull(
                            user, BudgetType.GLOBAL, request.getYear(), request.getMonth())
                    .isPresent();
            if (exists) {
                throw new BadRequestException(
                        "A global budget already exists for " + request.getYear() + "/" + request.getMonth());
            }
        } else {
            if (request.getCategoryId() == null) {
                throw new BadRequestException("Category ID is required for CATEGORY type budgets");
            }
            boolean exists = budgetRepository
                    .findByUserAndBudgetTypeAndYearAndMonthAndCategoryId(
                            user, BudgetType.CATEGORY, request.getYear(), request.getMonth(),
                            request.getCategoryId())
                    .isPresent();
            if (exists) {
                throw new BadRequestException(
                        "A budget for this category already exists for " + request.getYear() + "/" + request.getMonth());
            }
        }
    }
}
