package me.amjath.expense_tracker_api.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.category.dto.CategoryRequest;
import me.amjath.expense_tracker_api.category.dto.CategoryResponse;
import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.category.mapper.CategoryMapper;
import me.amjath.expense_tracker_api.category.repository.CategoryRepository;
import me.amjath.expense_tracker_api.common.PagedResponse;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.ForbiddenException;
import me.amjath.expense_tracker_api.exception.NotFoundException;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /** Get all system categories (public). */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSystemCategories() {
        return categoryRepository.findAllBySystemTrue()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /** Get all categories visible to the current user (system + own custom). */
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAllCategories(User user, Pageable pageable) {
        return PagedResponse.of(
                categoryRepository.findAllVisibleToUser(user, pageable)
                        .map(categoryMapper::toResponse)
        );
    }

    /** Get a specific category by ID (must be system or owned by user). */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id, User user) {
        Category category = findCategoryForUser(id, user);
        return categoryMapper.toResponse(category);
    }

    /** Create a custom category for the current user. */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        if (categoryRepository.existsByNameAndUserAndSystemFalse(request.getName(), user)) {
            throw new BadRequestException("A custom category with name '" + request.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setSystem(false);
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        log.info("Custom category created: {} for user: {}", saved.getName(), user.getEmail());
        return categoryMapper.toResponse(saved);
    }

    /** Update a custom category (only the owner can update). */
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request, User user) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Category", id));

        if (category.isSystem()) {
            throw new ForbiddenException("System categories cannot be modified");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        Category updated = categoryRepository.save(category);
        log.info("Category updated: {}", updated.getId());
        return categoryMapper.toResponse(updated);
    }

    /** Soft-delete a custom category (only the owner can delete). */
    @Transactional
    public void deleteCategory(UUID id, User user) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Category", id));

        if (category.isSystem()) {
            throw new ForbiddenException("System categories cannot be deleted");
        }

        category.softDelete();
        categoryRepository.save(category);
        log.info("Category soft-deleted: {}", id);
    }

    // ---------------------------------------------------------------
    // Package-accessible helper (used by other services)
    // ---------------------------------------------------------------

    public Category findCategoryForUser(UUID id, User user) {
        // Check system first, then user-owned
        return categoryRepository.findByIdAndSystemTrue(id)
                .or(() -> categoryRepository.findByIdAndUser(id, user))
                .orElseThrow(() -> new NotFoundException("Category", id));
    }
}
