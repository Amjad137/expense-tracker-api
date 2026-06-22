package me.amjath.expense_tracker_api.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReportResponse {

    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal totalAmount;
    private long transactionCount;
    private double percentageOfTotal;
}
