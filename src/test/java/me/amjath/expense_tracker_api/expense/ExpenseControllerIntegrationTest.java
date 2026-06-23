package me.amjath.expense_tracker_api.expense;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.GlobalExceptionHandler;
import me.amjath.expense_tracker_api.expense.controller.ExpenseController;
import me.amjath.expense_tracker_api.expense.dto.ExpenseRequest;
import me.amjath.expense_tracker_api.expense.dto.ExpenseResponse;
import me.amjath.expense_tracker_api.expense.service.ExpenseService;
import me.amjath.expense_tracker_api.security.JwtAuthenticationFilter;
import me.amjath.expense_tracker_api.security.JwtService;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpenseController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ExpenseController Integration Tests")
class ExpenseControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ExpenseService expenseService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    @DisplayName("GET /api/v1/expenses - 401 without authentication")
    void getExpenses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/expenses - 200 when authenticated")
    @WithMockUser(username = "user@example.com")
    void getExpenses_authenticated_returns200() throws Exception {
        var paged = PagedResponse.<ExpenseResponse>builder()
                .content(List.of())
                .page(0).size(20).totalElements(0).totalPages(0).first(true).last(true)
                .build();

        when(expenseService.getExpenses(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/v1/expenses")
                        .with(user(buildUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/expenses - 201 with valid request")
    @WithMockUser(username = "user@example.com")
    void createExpense_validRequest_returns201() throws Exception {
        var request = new ExpenseRequest();
        request.setTitle("Coffee");
        request.setAmount(new BigDecimal("4.50"));
        request.setExpenseDate(LocalDate.now());

        var response = ExpenseResponse.builder()
                .id(UUID.randomUUID())
                .title("Coffee")
                .amount(new BigDecimal("4.50"))
                .expenseDate(LocalDate.now())
                .build();

        when(expenseService.createExpense(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/expenses")
                        .with(user(buildUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Coffee"));
    }

    @Test
    @DisplayName("POST /api/v1/expenses - 400 when amount is missing")
    @WithMockUser(username = "user@example.com")
    void createExpense_missingAmount_returns400() throws Exception {
        var request = new ExpenseRequest();
        request.setTitle("Coffee");
        // amount missing
        request.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/expenses")
                        .with(user(buildUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
