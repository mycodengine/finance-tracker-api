package com.financetracker.controller;

import com.financetracker.domain.entity.User;
import com.financetracker.dto.request.CategoryRequest;
import com.financetracker.dto.response.CategoryResponse;
import com.financetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Manage transaction categories (system defaults + personal)")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List all visible categories (system defaults + personal)")
    public List<CategoryResponse> findAll(@AuthenticationPrincipal User user) {
        return categoryService.findAll(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a personal category")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request,
                                   @AuthenticationPrincipal User user) {
        return categoryService.create(request, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a personal category")
    public CategoryResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request,
                                   @AuthenticationPrincipal User user) {
        return categoryService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a personal category")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        categoryService.delete(id, user);
    }
}
