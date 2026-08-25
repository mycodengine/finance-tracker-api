package com.financetracker.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        String icon,
        boolean systemCategory
) {
}
