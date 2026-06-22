package me.amjath.expense_tracker_api.income.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.income.dto.IncomeRequest;
import me.amjath.expense_tracker_api.income.dto.IncomeResponse;
import me.amjath.expense_tracker_api.income.entity.Income;
import me.amjath.expense_tracker_api.income.mapper.IncomeMapper;
import me.amjath.expense_tracker_api.income.repository.IncomeRepository;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeMapper incomeMapper;

    @Transactional(readOnly = true)
    public PagedResponse<IncomeResponse> getIncomes(User user, Pageable pageable) {
        return PagedResponse.of(
                incomeRepository.findAllByUser(user, pageable).map(incomeMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(UUID id, User user) {
        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Income", id));
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest request, User user) {
        Income income = new Income();
        income.setUser(user);
        income.setTitle(request.getTitle());
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setIncomeDate(request.getIncomeDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            income.setCategory(category);
        }

        Income saved = incomeRepository.save(income);
        log.info("Income created: {} for user: {}", saved.getId(), user.getEmail());
        return incomeMapper.toResponse(saved);
    }

    @Transactional
    public IncomeResponse updateIncome(UUID id, IncomeRequest request, User user) {
        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Income", id));

        income.setTitle(request.getTitle());
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setIncomeDate(request.getIncomeDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            income.setCategory(category);
        } else {
            income.setCategory(null);
        }

        Income updated = incomeRepository.save(income);
        log.info("Income updated: {}", id);
        return incomeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteIncome(UUID id, User user) {
        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Income", id));
        income.softDelete();
        incomeRepository.save(income);
        log.info("Income soft-deleted: {}", id);
    }
}
