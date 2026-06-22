package me.amjath.expense_tracker_api.expense.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.expense.dto.ExpenseRequest;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.expense.entity.Expense;
import me.amjath.expense_tracker_api.expense.mapper.ExpenseMapper;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.expense.repository.ExpenseSpecification;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getExpenses(
            User user,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String search,
            Pageable pageable) {

        Specification<Expense> spec = ExpenseSpecification.buildSpec(
                user, categoryId, startDate, endDate, minAmount, maxAmount, search);

        return PagedResponse.of(
                expenseRepository.findAll(spec, pageable).map(expenseMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id, User user) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Expense", id));
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, User user) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            expense.setCategory(category);
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created: {} for user: {}", saved.getId(), user.getEmail());
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID id, ExpenseRequest request, User user) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Expense", id));

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            expense.setCategory(category);
        } else {
            expense.setCategory(null);
        }

        Expense updated = expenseRepository.save(expense);
        log.info("Expense updated: {}", id);
        return expenseMapper.toResponse(updated);
    }

    @Transactional
    public void deleteExpense(UUID id, User user) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Expense", id));
        expense.softDelete();
        expenseRepository.save(expense);
        log.info("Expense soft-deleted: {}", id);
    }
}
