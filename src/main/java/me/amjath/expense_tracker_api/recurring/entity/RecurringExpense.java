package me.amjath.expense_tracker_api.recurring.entity;

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
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.common.BaseEntity;
import me.amjath.expense_tracker_api.recurring.enums.Frequency;
import me.amjath.expense_tracker_api.user.entity.User;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recurring_expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class RecurringExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // ---------------------------------------------------------------
    // Domain logic
    // ---------------------------------------------------------------

    /**
     * Calculates the next due date from a given base date using the frequency.
     */
    public LocalDate calculateNextDueDate(LocalDate baseDate) {
        return switch (frequency) {
            case DAILY   -> baseDate.plusDays(1);
            case WEEKLY  -> baseDate.plusWeeks(1);
            case MONTHLY -> baseDate.plusMonths(1);
            case YEARLY  -> baseDate.plusYears(1);
        };
    }
}
