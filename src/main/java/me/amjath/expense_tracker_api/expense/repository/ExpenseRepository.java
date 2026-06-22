package me.amjath.expense_tracker_api.expense.repository;

import me.amjath.expense_tracker_api.expense.entity.Expense;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>,
        JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUser(UUID id, User user);

    Page<Expense> findAllByUser(User user, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user = :user
              AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user = :user
              AND e.category.id = :categoryId
              AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByUserAndCategoryAndDateRange(
            User user, UUID categoryId, LocalDate startDate, LocalDate endDate);
}
