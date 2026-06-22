package me.amjath.expense_tracker_api.report.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryAmountSummary {
    UUID getCategoryId();

    String getCategoryName();

    String getCategoryIcon();

    String getCategoryColor();

    BigDecimal getTotalAmount();

    Long getTransactionCount();
}
