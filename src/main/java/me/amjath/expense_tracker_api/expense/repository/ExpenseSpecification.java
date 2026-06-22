package me.amjath.expense_tracker_api.expense.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import me.amjath.expense_tracker_api.expense.entity.Expense;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic expense filtering.
 * Combine these with Specification.where() to build complex queries.
 */
public class ExpenseSpecification {

    private ExpenseSpecification() {
        // Utility class
    }

    public static Specification<Expense> buildSpec(
            User user,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user
            predicates.add(cb.equal(root.get("user"), user));

            // Category filter
            if (categoryId != null) {
                Join<?, ?> categoryJoin = root.join("category", JoinType.LEFT);
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }

            // Date range
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
            }

            // Amount range
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            // Full-text search on title or description
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
