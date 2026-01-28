package com.elseeker.game.adapter.input.api.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "O/X 퀴즈 답안 제출 요청")
data class BibleOxQuizAnswerRequest(
    @field:NotNull(message = "선택한 답안은 필수입니다")
    @field:Schema(description = "선택한 답안 (true=O, false=X)", example = "true")
    val selectedAnswer: Boolean
)
