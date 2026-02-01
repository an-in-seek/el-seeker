package com.elseeker.bible.adapter.input.api.admin.request

import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType

data class AdminBibleBookRequest(
    val bookKey: BibleBookKey,
    val bookOrder: Int,
    val name: String,
    val abbreviation: String,
    val testamentType: BibleTestamentType,
)
