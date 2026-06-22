package me.amjath.expense_tracker_api.category;

import me.amjath.expense_tracker_api.category.dto.CategoryRequest;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.mapper.CategoryMapper;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.category.service.CategoryService;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.ForbiddenException;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    private User testUser;
    private Category systemCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");
        testUser.setRole(Role.USER);

        systemCategory = new Category();
        systemCategory.setId(UUID.randomUUID());
        systemCategory.setName("Food & Dining");
        systemCategory.setSystem(true);

        customCategory = new Category();
        customCategory.setId(UUID.randomUUID());
        customCategory.setName("My Custom");
        customCategory.setSystem(false);
        customCategory.setUser(testUser);
    }

    @Test
    @DisplayName("getSystemCategories - returns all system categories")
    void getSystemCategories_returnsAll() {
        when(categoryRepository.findAllBySystemTrue()).thenReturn(List.of(systemCategory));

        var result = categoryService.getSystemCategories();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("createCategory - duplicate name throws BadRequestException")
    void createCategory_duplicateName_throws() {
        var request = new CategoryRequest();
        request.setName("My Custom");

        when(categoryRepository.existsByNameAndUserAndSystemFalse(anyString(), any())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request, testUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("deleteCategory - system category throws ForbiddenException")
    void deleteCategory_system_throwsForbidden() {
        when(categoryRepository.findByIdAndUser(systemCategory.getId(), testUser))
                .thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory(systemCategory.getId(), testUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("deleteCategory - custom category succeeds with soft delete")
    void deleteCategory_custom_softDeletes() {
        when(categoryRepository.findByIdAndUser(customCategory.getId(), testUser))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.save(any())).thenReturn(customCategory);

        categoryService.deleteCategory(customCategory.getId(), testUser);

        assertThat(customCategory.isDeleted()).isTrue();
        verify(categoryRepository).save(customCategory);
    }
}
