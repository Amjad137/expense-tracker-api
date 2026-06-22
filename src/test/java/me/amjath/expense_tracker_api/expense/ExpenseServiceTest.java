package me.amjath.expense_tracker_api.expense;

import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.expense.dto.ExpenseRequest;
import me.amjath.expense_tracker_api.expense.entity.Expense;
import me.amjath.expense_tracker_api.expense.mapper.ExpenseMapper;
import me.amjath.expense_tracker_api.expense.repository.ExpenseRepository;
import me.amjath.expense_tracker_api.expense.service.ExpenseService;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Unit Tests")
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ExpenseMapper expenseMapper;

    @InjectMocks private ExpenseService expenseService;

    private User testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");
        testUser.setRole(Role.USER);

        testExpense = new Expense();
        testExpense.setId(UUID.randomUUID());
        testExpense.setUser(testUser);
        testExpense.setTitle("Grocery Shopping");
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setExpenseDate(LocalDate.now());
    }

    @Test
    @DisplayName("createExpense - success without category")
    void createExpense_success() {
        var request = new ExpenseRequest();
        request.setTitle("Grocery Shopping");
        request.setAmount(new BigDecimal("50.00"));
        request.setExpenseDate(LocalDate.now());

        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        expenseService.createExpense(request, testUser);

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("getExpenseById - throws NotFoundException for non-existent expense")
    void getExpenseById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(expenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(id, testUser))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteExpense - soft deletes the expense")
    void deleteExpense_softDelete() {
        UUID id = testExpense.getId();
        when(expenseRepository.findByIdAndUser(id, testUser)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        expenseService.deleteExpense(id, testUser);

        assertThat(testExpense.isDeleted()).isTrue();
        assertThat(testExpense.getDeletedAt()).isNotNull();
        verify(expenseRepository).save(testExpense);
    }

    @Test
    @DisplayName("getExpenses - returns paged result")
    void getExpenses_returnsPagedResult() {
        var page = new PageImpl<>(List.of(testExpense), PageRequest.of(0, 20), 1);

        when(expenseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        var result = expenseService.getExpenses(testUser, null, null, null, null, null, null,
                PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
