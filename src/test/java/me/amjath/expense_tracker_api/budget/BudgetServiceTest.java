package me.amjath.expense_tracker_api.budget;

import me.amjath.expense_tracker_api.budget.dto.BudgetRequest;
import me.amjath.expense_tracker_api.budget.entity.Budget;
import me.amjath.expense_tracker_api.budget.enums.BudgetStatus;
import me.amjath.expense_tracker_api.budget.enums.BudgetType;
import me.amjath.expense_tracker_api.budget.mapper.BudgetMapper;
import me.amjath.expense_tracker_api.budget.repository.BudgetRepository;
import me.amjath.expense_tracker_api.budget.service.BudgetService;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService Unit Tests")
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private BudgetMapper budgetMapper;

    @InjectMocks private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");
        testUser.setRole(Role.USER);

        testBudget = new Budget();
        testBudget.setId(UUID.randomUUID());
        testBudget.setUser(testUser);
        testBudget.setBudgetType(BudgetType.GLOBAL);
        testBudget.setLimitAmount(new BigDecimal("1000.00"));
        testBudget.setYear(LocalDate.now().getYear());
        testBudget.setMonth(LocalDate.now().getMonthValue());
    }

    // ---------------------------------------------------------------
    // Status calculation tests (via BudgetMapper directly)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("BudgetMapper - status OK when spent < 80%")
    void budgetMapper_statusOk() {
        BudgetMapper mapper = new BudgetMapper();
        var response = mapper.toResponse(testBudget, new BigDecimal("700.00")); // 70%
        assertThat(response.getStatus()).isEqualTo(BudgetStatus.OK);
    }

    @Test
    @DisplayName("BudgetMapper - status WARNING when spent >= 80%")
    void budgetMapper_statusWarning() {
        BudgetMapper mapper = new BudgetMapper();
        var response = mapper.toResponse(testBudget, new BigDecimal("850.00")); // 85%
        assertThat(response.getStatus()).isEqualTo(BudgetStatus.WARNING);
    }

    @Test
    @DisplayName("BudgetMapper - status EXCEEDED when spent >= 100%")
    void budgetMapper_statusExceeded() {
        BudgetMapper mapper = new BudgetMapper();
        var response = mapper.toResponse(testBudget, new BigDecimal("1100.00")); // 110%
        assertThat(response.getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
    }

    @Test
    @DisplayName("BudgetMapper - status EXCEEDED when spent exactly 100%")
    void budgetMapper_statusExceededAtExact100() {
        BudgetMapper mapper = new BudgetMapper();
        var response = mapper.toResponse(testBudget, new BigDecimal("1000.00")); // 100%
        assertThat(response.getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
    }

    // ---------------------------------------------------------------
    // Duplicate budget validation tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("createBudget - throws when global budget already exists for month")
    void createBudget_duplicateGlobal_throws() {
        var request = new BudgetRequest();
        request.setBudgetType(BudgetType.GLOBAL);
        request.setLimitAmount(new BigDecimal("500.00"));
        request.setYear(LocalDate.now().getYear());
        request.setMonth(LocalDate.now().getMonthValue());

        when(budgetRepository.findByUserAndBudgetTypeAndYearAndMonthAndCategoryIsNull(
                any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> budgetService.createBudget(request, testUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("global budget already exists");
    }
}
