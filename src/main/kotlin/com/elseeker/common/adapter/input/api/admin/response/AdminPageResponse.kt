package com.elseeker.common.adapter.input.api.admin.response

import org.springframework.data.domain.Page

data class AdminPageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
) {
    companion object {
        fun <T, R> from(page: Page<T>, mapper: (T) -> R): AdminPageResponse<R> =
            AdminPageResponse(
                content = page.content.map(mapper),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious(),
            )
    }
}
