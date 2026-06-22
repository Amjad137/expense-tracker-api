package me.amjath.expense_tracker_api.recurring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.recurring.dto.RecurringExpenseRequest;
import me.amjath.expense_tracker_api.recurring.dto.RecurringExpenseResponse;
import me.amjath.expense_tracker_api.recurring.service.RecurringExpenseService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Recurring Expenses", description = "Recurring expense templates with simulation support")
@RestController
@RequestMapping("/api/v1/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService service;

    @Operation(summary = "List recurring expenses")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RecurringExpenseResponse>>> getAll(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getAll(currentUser, activeOnly,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nextDueDate")))));
    }

    @Operation(summary = "Get recurring expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id, currentUser)));
    }

    @Operation(summary = "Create a recurring expense definition")
    @PostMapping
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> create(
            @Valid @RequestBody RecurringExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring expense created",
                        service.create(request, currentUser)));
    }

    @Operation(summary = "Update a recurring expense definition")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecurringExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Recurring expense updated",
                service.update(id, request, currentUser)));
    }

    @Operation(summary = "Delete a recurring expense definition")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        service.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Recurring expense deleted"));
    }

    @Operation(summary = "Simulate future occurrences (read-only preview, nothing is persisted)")
    @GetMapping("/{id}/simulate")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> simulate(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until) {
        return ResponseEntity.ok(ApiResponse.success(
                "Simulation complete (no expenses were created)",
                service.simulate(id, currentUser, until)));
    }
}
