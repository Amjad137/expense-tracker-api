package me.amjath.expense_tracker_api.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.amjath.expense_tracker_api.category.dto.CategoryRequest;
import me.amjath.expense_tracker_api.category.dto.CategoryResponse;
import me.amjath.expense_tracker_api.category.service.CategoryService;
import me.amjath.expense_tracker_api.common.ApiResponse;
import me.amjath.expense_tracker_api.common.PagedResponse;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "System and custom category management")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all system categories (public)")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSystemCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSystemCategories()));
    }

    @Operation(summary = "Get all visible categories (system + user custom)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getAllCategories(currentUser,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort)))));
    }

    @Operation(summary = "Get a category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id, currentUser)));
    }

    @Operation(summary = "Create a custom category")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully",
                        categoryService.createCategory(request, currentUser)));
    }

    @Operation(summary = "Update a custom category")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully",
                categoryService.updateCategory(id, request, currentUser)));
    }

    @Operation(summary = "Delete a custom category")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        categoryService.deleteCategory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
