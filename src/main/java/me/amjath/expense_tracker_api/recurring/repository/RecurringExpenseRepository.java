package me.amjath.expense_tracker_api.recurring.repository;

import me.amjath.expense_tracker_api.recurring.entity.RecurringExpense;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    Optional<RecurringExpense> findByIdAndUser(UUID id, User user);

    Page<RecurringExpense> findAllByUser(User user, Pageable pageable);

    Page<RecurringExpense> findAllByUserAndActiveTrue(User user, Pageable pageable);
}
