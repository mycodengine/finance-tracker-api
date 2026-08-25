package com.financetracker.mapper;

import com.financetracker.domain.entity.Category;
import com.financetracker.dto.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "systemCategory", expression = "java(category.isSystemCategory())")
    CategoryResponse toResponse(Category category);
}
