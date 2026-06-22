package me.amjath.expense_tracker_api.recurring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.amjath.expense_tracker_api.recurring.enums.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseResponse {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private String description;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate nextDueDate;
    private LocalDate endDate;
    private boolean active;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
