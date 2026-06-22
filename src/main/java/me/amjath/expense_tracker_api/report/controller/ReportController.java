package me.amjath.expense_tracker_api.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.report.dto.CategoryReportResponse;
import me.amjath.expense_tracker_api.report.dto.MonthlyReportResponse;
import me.amjath.expense_tracker_api.report.service.ReportService;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports", description = "Financial reports — monthly summaries and category breakdowns")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Get monthly financial report (income, expense, category breakdown)")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyReport(currentUser, y, m)));
    }

    @Operation(summary = "Get expense breakdown by category for a given month")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryReportResponse>>> getCategoryReport(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(reportService.getCategoryReport(currentUser, y, m)));
    }
}
