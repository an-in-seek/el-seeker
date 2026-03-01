package com.elseeker.game.adapter.input.api.admin.request

import com.elseeker.game.domain.vo.ClueType
import com.elseeker.game.domain.vo.PuzzleDirection

data class AdminWordPuzzleEntryRequest(
    val dictionaryId: Long,
    val answerText: String,
    val directionCode: PuzzleDirection,
    val startRow: Int,
    val startCol: Int,
    val clueNumber: Int,
    val clueTypeCode: ClueType,
    val clueText: String,
)
