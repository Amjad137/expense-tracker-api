package me.amjath.expense_tracker_api.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.budget.dto.BudgetRequest;
import me.amjath.expense_tracker_api.budget.dto.BudgetResponse;
import me.amjath.expense_tracker_api.budget.service.BudgetService;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Budgets", description = "Monthly budget management with real-time spending status")
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Get all budgets with spending status")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.getBudgets(currentUser,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "year", "month")))));
    }

    @Operation(summary = "Get budgets for a specific month")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsForMonth(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgetsForMonth(currentUser, y, m)));
    }

    @Operation(summary = "Get a budget by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgetById(id, currentUser)));
    }

    @Operation(summary = "Create a new budget")
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully",
                        budgetService.createBudget(request, currentUser)));
    }

    @Operation(summary = "Update a budget limit")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully",
                budgetService.updateBudget(id, request, currentUser)));
    }

    @Operation(summary = "Delete a budget")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudget(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }
}
