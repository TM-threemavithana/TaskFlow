package com.taskflow.dto;

import java.util.List;

/**
 * GENERIC PAGED RESPONSE.
 *
 * <p>GENERICS (the {@code <T>}): this single class works for any content type -
 * {@code PagedResponse<TaskResponse>}, {@code PagedResponse<ProjectResponse>}, etc.
 * The compiler enforces type safety: you can't accidentally put a ProjectResponse into a
 * PagedResponse that was declared as {@code PagedResponse<TaskResponse>}.
 *
 * @param <T> the type of items in this page
 */
public record PagedResponse<T>(
        /** The items on this page. */
        List<T> content,
        /** Current page number (0-based). */
        int page,
        /** Number of items per page. */
        int size,
        /** Total items across all pages. */
        long totalElements,
        /** Total number of pages. */
        int totalPages,
        /** True if this is the last page. */
        boolean last
) {}
