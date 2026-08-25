package com.financetracker.service;

import com.financetracker.domain.entity.Category;
import com.financetracker.domain.entity.User;
import com.financetracker.dto.request.CategoryRequest;
import com.financetracker.dto.response.CategoryResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.CategoryMapper;
import com.financetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /** Returns system defaults + the user's personal categories. */
    public List<CategoryResponse> findAll(User user) {
        return categoryRepository.findAllVisibleToUser(user).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request, User user) {
        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .color(request.color() != null ? request.color() : "#6366F1")
                .icon(request.icon())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category '{}' created for user {}", saved.getName(), user.getEmail());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request, User user) {
        Category category = getPersonalOrThrow(id, user);
        category.setName(request.name());
        if (request.color() != null) {
            category.setColor(request.color());
        }
        category.setIcon(request.icon());

        log.info("Category {} updated for user {}", id, user.getEmail());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id, User user) {
        Category category = getPersonalOrThrow(id, user);
        categoryRepository.delete(category);
        log.info("Category {} deleted for user {}", id, user.getEmail());
    }

    /** Only personal (non-system) categories belonging to this user can be modified. */
    private Category getPersonalOrThrow(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal category not found: " + id));
    }
}
