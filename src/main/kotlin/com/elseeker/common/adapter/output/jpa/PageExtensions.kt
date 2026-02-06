package com.elseeker.common.adapter.output.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

fun <T : Any> Page<T?>.filterNotNullPage(): Page<T> {
    val filteredContent = this.content.filterNotNull()
    return PageImpl(filteredContent, this.pageable, this.totalElements)
}

fun <T : Any> Slice<T?>.filterNotNullSlice(): Slice<T> {
    val filteredContent = this.content.filterNotNull()
    return SliceImpl(filteredContent, this.pageable, this.hasNext())
}