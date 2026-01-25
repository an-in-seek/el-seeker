package com.elseeker.game.adapter.input.api.response

import io.swagger.v3.oas.annotations.media.Schema

data class QuizStageResponse(
    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,
    @field:Schema(description = "스테이지 제목", example = "창세기 퀴즈", nullable = true)
    val title: String?,
    val questions: List<QuizQuestionResponse>,
    @field:Schema(description = "전체 스테이지 수", example = "10")
    val stageCount: Int,
    @field:Schema(description = "문제 수", example = "10")
    val questionCount: Int,
    val progress: QuizStageProgressResponse
)

data class QuizQuestionResponse(
    @field:Schema(description = "문제 ID", example = "101")
    val id: Long,
    @field:Schema(description = "문제 내용", example = "아담의 첫째 아들의 이름은?")
    val question: String,
    @field:Schema(description = "보기 목록", example = "[\"가인\", \"아벨\", \"셋\", \"에녹\"]")
    val options: List<String>
)
