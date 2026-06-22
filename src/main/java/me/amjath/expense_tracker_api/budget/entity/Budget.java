package me.amjath.expense_tracker_api.budget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.common.BaseEntity;
import me.amjath.expense_tracker_api.user.entity.User;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Budget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Null for GLOBAL budgets, set for CATEGORY budgets. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false)
    private BudgetType budgetType;

    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;
}
