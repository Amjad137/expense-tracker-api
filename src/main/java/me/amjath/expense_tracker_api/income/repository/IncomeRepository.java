package me.amjath.expense_tracker_api.income.repository;

import me.amjath.expense_tracker_api.income.entity.Income;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomeRepository extends JpaRepository<Income, UUID> {

    Optional<Income> findByIdAndUser(UUID id, User user);

    Page<Income> findAllByUser(User user, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM Income i
            WHERE i.user = :user
              AND i.incomeDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate);

    List<Income> findTop5ByUserOrderByIncomeDateDesc(User user);
}
