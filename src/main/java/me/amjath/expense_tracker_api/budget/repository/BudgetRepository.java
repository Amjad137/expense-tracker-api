package me.amjath.expense_tracker_api.budget.repository;

import me.amjath.expense_tracker_api.budget.entity.Budget;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByIdAndUser(UUID id, User user);

    Page<Budget> findAllByUser(User user, Pageable pageable);

    List<Budget> findAllByUserAndYearAndMonth(User user, int year, int month);

    Optional<Budget> findByUserAndBudgetTypeAndYearAndMonthAndCategoryIsNull(
            User user, BudgetType budgetType, int year, int month);

    Optional<Budget> findByUserAndBudgetTypeAndYearAndMonthAndCategoryId(
            User user, BudgetType budgetType, int year, int month, UUID categoryId);
}
