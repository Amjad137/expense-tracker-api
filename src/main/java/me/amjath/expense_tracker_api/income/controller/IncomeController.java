package me.amjath.expense_tracker_api.income.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.income.dto.IncomeRequest;
import me.amjath.expense_tracker_api.income.dto.IncomeResponse;
import me.amjath.expense_tracker_api.income.service.IncomeService;
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

import java.util.UUID;

@Tag(name = "Incomes", description = "Income management with pagination")
@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @Operation(summary = "Get all incomes")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<IncomeResponse>>> getIncomes(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "incomeDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return ResponseEntity.ok(ApiResponse.success(
                incomeService.getIncomes(currentUser, PageRequest.of(page, size, Sort.by(dir, sort)))));
    }

    @Operation(summary = "Get income by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> getIncomeById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(incomeService.getIncomeById(id, currentUser)));
    }

    @Operation(summary = "Create a new income entry")
    @PostMapping
    public ResponseEntity<ApiResponse<IncomeResponse>> createIncome(
            @Valid @RequestBody IncomeRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income created successfully",
                        incomeService.createIncome(request, currentUser)));
    }

    @Operation(summary = "Update an income entry")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            @PathVariable UUID id,
            @Valid @RequestBody IncomeRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Income updated successfully",
                incomeService.updateIncome(id, request, currentUser)));
    }

    @Operation(summary = "Soft-delete an income entry")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        incomeService.deleteIncome(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Income deleted successfully"));
    }
}
