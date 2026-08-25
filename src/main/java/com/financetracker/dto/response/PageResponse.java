package com.financetracker.dto.response;

import java.util.List;

/** Generic paginated wrapper returned by list endpoints. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
