package me.amjath.expense_tracker_api.expense.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.expense.dto.ExpenseRequest;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.expense.service.ExpenseService;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Expenses", description = "Expense tracking with filtering, pagination, and soft delete")
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get all expenses with optional filters")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getExpenses(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.getExpenses(currentUser, categoryId, startDate, endDate,
                        minAmount, maxAmount, search,
                        PageRequest.of(page, size, Sort.by(dir, sort)))));
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenseById(id, currentUser)));
    }

    @Operation(summary = "Create a new expense")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully",
                        expenseService.createExpense(request, currentUser)));
    }

    @Operation(summary = "Update an expense")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully",
                expenseService.updateExpense(id, request, currentUser)));
    }

    @Operation(summary = "Soft-delete an expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        expenseService.deleteExpense(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully"));
    }
}
