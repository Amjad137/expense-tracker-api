package me.amjath.expense_tracker_api.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
