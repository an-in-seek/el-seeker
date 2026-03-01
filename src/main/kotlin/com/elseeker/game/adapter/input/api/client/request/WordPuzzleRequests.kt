package com.elseeker.game.adapter.input.api.client.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "셀 저장 요청")
data class CellSaveRequest(
    @field:NotNull(message = "셀 목록은 필수입니다")
    @field:Valid
    @field:Size(min = 1, message = "최소 1개의 셀이 필요합니다")
    @field:Schema(description = "변경된 셀 목록")
    val cells: List<CellInput>,

    @field:NotNull(message = "경과 시간은 필수입니다")
    @field:Min(value = 0, message = "경과 시간은 0 이상이어야 합니다")
    @field:Schema(description = "클라이언트 측 누적 경과 시간 (초)", example = "45")
    val elapsedSeconds: Int
)

@Schema(description = "셀 입력 정보")
data class CellInput(
    @field:NotNull(message = "행 인덱스는 필수입니다")
    @field:Min(value = 0, message = "행 인덱스는 0 이상이어야 합니다")
    @field:Schema(description = "행 인덱스", example = "0")
    val row: Int,

    @field:NotNull(message = "열 인덱스는 필수입니다")
    @field:Min(value = 0, message = "열 인덱스는 0 이상이어야 합니다")
    @field:Schema(description = "열 인덱스", example = "2")
    val col: Int,

    @field:Schema(description = "입력된 글자. null이면 삭제", example = "창")
    val letter: String?
)

@Schema(description = "글자 공개 힌트 요청")
data class RevealLetterRequest(
    @field:NotNull(message = "entry ID는 필수입니다")
    @field:Schema(description = "해당 단어의 entry ID", example = "1")
    val entryId: Long,

    @field:NotNull(message = "행 인덱스는 필수입니다")
    @field:Min(value = 0, message = "행 인덱스는 0 이상이어야 합니다")
    @field:Schema(description = "행 인덱스", example = "0")
    val row: Int,

    @field:NotNull(message = "열 인덱스는 필수입니다")
    @field:Min(value = 0, message = "열 인덱스는 0 이상이어야 합니다")
    @field:Schema(description = "열 인덱스", example = "2")
    val col: Int,

    @field:NotNull(message = "경과 시간은 필수입니다")
    @field:Min(value = 0, message = "경과 시간은 0 이상이어야 합니다")
    @field:Schema(description = "누적 경과 시간 (초)", example = "60")
    val elapsedSeconds: Int
)

@Schema(description = "단어 확인 힌트 요청")
data class CheckWordRequest(
    @field:NotNull(message = "entry ID는 필수입니다")
    @field:Schema(description = "확인할 단어의 entry ID", example = "1")
    val entryId: Long,

    @field:NotNull(message = "경과 시간은 필수입니다")
    @field:Min(value = 0, message = "경과 시간은 0 이상이어야 합니다")
    @field:Schema(description = "누적 경과 시간 (초)", example = "90")
    val elapsedSeconds: Int
)

@Schema(description = "전체 제출 요청")
data class SubmitRequest(
    @field:NotNull(message = "경과 시간은 필수입니다")
    @field:Min(value = 0, message = "경과 시간은 0 이상이어야 합니다")
    @field:Schema(description = "누적 경과 시간 (초)", example = "300")
    val elapsedSeconds: Int
)
