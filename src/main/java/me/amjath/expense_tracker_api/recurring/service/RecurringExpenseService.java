package me.amjath.expense_tracker_api.recurring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.expense.entity.Expense;
import me.amjath.expense_tracker_api.expense.mapper.ExpenseMapper;
import me.amjath.expense_tracker_api.recurring.dto.RecurringExpenseRequest;
import me.amjath.expense_tracker_api.recurring.dto.RecurringExpenseResponse;
import me.amjath.expense_tracker_api.recurring.entity.RecurringExpense;
import me.amjath.expense_tracker_api.recurring.mapper.RecurringExpenseMapper;
import me.amjath.expense_tracker_api.recurring.repository.RecurringExpenseRepository;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringExpenseMapper mapper;
    private final ExpenseMapper expenseMapper;

    @Transactional(readOnly = true)
    public PagedResponse<RecurringExpenseResponse> getAll(User user, boolean activeOnly, Pageable pageable) {
        var page = activeOnly
                ? recurringRepository.findAllByUserAndActiveTrue(user, pageable)
                : recurringRepository.findAllByUser(user, pageable);
        return PagedResponse.of(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public RecurringExpenseResponse getById(UUID id, User user) {
        RecurringExpense re = recurringRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("RecurringExpense", id));
        return mapper.toResponse(re);
    }

    @Transactional
    public RecurringExpenseResponse create(RecurringExpenseRequest request, User user) {
        RecurringExpense re = new RecurringExpense();
        re.setUser(user);
        re.setTitle(request.getTitle());
        re.setAmount(request.getAmount());
        re.setDescription(request.getDescription());
        re.setFrequency(request.getFrequency());
        re.setStartDate(request.getStartDate());
        re.setEndDate(request.getEndDate());
        re.setActive(request.isActive());
        re.setNextDueDate(request.getStartDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            re.setCategory(category);
        }

        RecurringExpense saved = recurringRepository.save(re);
        log.info("RecurringExpense created: {} for user: {}", saved.getId(), user.getEmail());
        return mapper.toResponse(saved);
    }

    @Transactional
    public RecurringExpenseResponse update(UUID id, RecurringExpenseRequest request, User user) {
        RecurringExpense re = recurringRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("RecurringExpense", id));

        re.setTitle(request.getTitle());
        re.setAmount(request.getAmount());
        re.setDescription(request.getDescription());
        re.setFrequency(request.getFrequency());
        re.setStartDate(request.getStartDate());
        re.setEndDate(request.getEndDate());
        re.setActive(request.isActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            re.setCategory(category);
        } else {
            re.setCategory(null);
        }

        RecurringExpense updated = recurringRepository.save(re);
        log.info("RecurringExpense updated: {}", id);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(UUID id, User user) {
        RecurringExpense re = recurringRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("RecurringExpense", id));
        re.softDelete();
        recurringRepository.save(re);
        log.info("RecurringExpense soft-deleted: {}", id);
    }

    /**
     * Simulates what expenses would be generated from the given recurring definition
     * between today and a future date (up to 12 occurrences max).
     * Does NOT persist anything.
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> simulate(UUID id, User user, LocalDate until) {
        RecurringExpense re = recurringRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("RecurringExpense", id));

        List<ExpenseResponse> simulated = new ArrayList<>();
        LocalDate current = re.getNextDueDate() != null ? re.getNextDueDate() : re.getStartDate();
        LocalDate limit = until != null ? until : LocalDate.now().plusMonths(3);
        int maxOccurrences = 50;
        int count = 0;

        while (!current.isAfter(limit) && count < maxOccurrences) {
            if (re.getEndDate() != null && current.isAfter(re.getEndDate())) break;

            // Build a transient Expense for projection (not saved)
            Expense expense = new Expense();
            expense.setTitle(re.getTitle());
            expense.setAmount(re.getAmount());
            expense.setDescription("[SIMULATED] " + (re.getDescription() != null ? re.getDescription() : ""));
            expense.setExpenseDate(current);
            expense.setUser(user);
            expense.setCategory(re.getCategory());

            simulated.add(expenseMapper.toResponse(expense));
            current = re.calculateNextDueDate(current);
            count++;
        }

        log.info("Simulated {} occurrences for recurring expense: {}", simulated.size(), id);
        return simulated;
    }
}
