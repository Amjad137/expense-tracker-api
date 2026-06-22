package me.amjath.expense_tracker_api.recurring.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import me.amjath.expense_tracker_api.recurring.enums.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecurringExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /** Optional — if null, runs indefinitely. */
    private LocalDate endDate;

    private UUID categoryId;

    private boolean active = true;
}
