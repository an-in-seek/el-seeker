package com.elseeker.game.adapter.input.api.client.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

// ── 퍼즐 목록 ──

@Schema(description = "퍼즐 목록 항목 응답")
data class PuzzleSummaryResponse(
    @field:Schema(description = "퍼즐 ID", example = "1")
    val puzzleId: Long,

    @field:Schema(description = "퍼즐 제목", example = "창세기 인물 퍼즐")
    val title: String,

    @field:Schema(description = "테마 코드", example = "GENESIS")
    val themeCode: String,

    @field:Schema(description = "난이도 코드", example = "EASY")
    val difficultyCode: String,

    @field:Schema(description = "보드 가로 크기", example = "10")
    val boardWidth: Int,

    @field:Schema(description = "보드 세로 크기", example = "10")
    val boardHeight: Int,

    @field:Schema(description = "게시일")
    val publishedAt: Instant?,

    @field:Schema(description = "진행 중인 attempt ID. null이면 신규, 값이 있으면 이어하기 가능")
    val inProgressAttemptId: Long?
)

// ── 퍼즐 시작 / 이어하기 ──

@Schema(description = "퍼즐 시도 응답 (보드 + 단서 + 셀)")
data class PuzzleAttemptResponse(
    @field:Schema(description = "attempt ID", example = "42")
    val attemptId: Long,

    @field:Schema(description = "퍼즐 제목", example = "창세기 인물 퍼즐")
    val title: String,

    @field:Schema(description = "누적 경과 시간 (초)", example = "0")
    val elapsedSeconds: Int,

    @field:Schema(description = "보드 크기 정보")
    val board: BoardResponse,

    @field:Schema(description = "단서 목록")
    val entries: List<EntryResponse>,

    @field:Schema(description = "셀 목록")
    val cells: List<CellResponse>
)

@Schema(description = "보드 크기 정보")
data class BoardResponse(
    @field:Schema(description = "보드 가로 크기", example = "10")
    val width: Int,

    @field:Schema(description = "보드 세로 크기", example = "10")
    val height: Int
)

@Schema(description = "단서 항목 응답")
data class EntryResponse(
    @field:Schema(description = "entry ID", example = "1")
    val entryId: Long,

    @field:Schema(description = "단서 번호", example = "1")
    val clueNumber: Int,

    @field:Schema(description = "방향 코드 (ACROSS / DOWN)", example = "ACROSS")
    val directionCode: String,

    @field:Schema(description = "시작 행 인덱스", example = "0")
    val startRow: Int,

    @field:Schema(description = "시작 열 인덱스", example = "2")
    val startCol: Int,

    @field:Schema(description = "단어 길이", example = "3")
    val length: Int,

    @field:Schema(description = "단서 유형 코드 (DEFINITION / VERSE)", example = "DEFINITION")
    val clueTypeCode: String,

    @field:Schema(description = "단서 텍스트", example = "하나님이 세상을 만드신 행위")
    val clueText: String
)

@Schema(description = "셀 응답")
data class CellResponse(
    @field:Schema(description = "행 인덱스", example = "0")
    val row: Int,

    @field:Schema(description = "열 인덱스", example = "2")
    val col: Int,

    @field:Schema(description = "입력된 글자", example = "창")
    val inputLetter: String?,

    @field:Schema(description = "힌트로 공개된 칸 여부", example = "false")
    val isRevealed: Boolean
)

// ── 글자 공개 힌트 ──

@Schema(description = "글자 공개 힌트 응답")
data class RevealLetterResponse(
    @field:Schema(description = "공개된 셀 행", example = "0")
    val row: Int,

    @field:Schema(description = "공개된 셀 열", example = "2")
    val col: Int,

    @field:Schema(description = "공개된 정답 글자", example = "창")
    val letter: String,

    @field:Schema(description = "누적 힌트 사용 횟수", example = "3")
    val hintUsageCount: Int
)

// ── 단어 확인 힌트 ──

@Schema(description = "단어 확인 힌트 응답")
data class CheckWordResponse(
    @field:Schema(description = "셀별 정답 여부")
    val results: List<CellCheckResult>,

    @field:Schema(description = "누적 힌트 사용 횟수", example = "4")
    val hintUsageCount: Int
)

@Schema(description = "셀 정답 확인 결과")
data class CellCheckResult(
    @field:Schema(description = "행 인덱스", example = "0")
    val row: Int,

    @field:Schema(description = "열 인덱스", example = "2")
    val col: Int,

    @field:Schema(description = "정답 여부", example = "true")
    val correct: Boolean
)

// ── 전체 제출 ──

@Schema(description = "전체 제출 정답 응답")
data class SubmitCorrectResponse(
    @field:Schema(description = "결과", example = "CORRECT")
    val result: String = "CORRECT",

    @field:Schema(description = "최종 점수", example = "850")
    val score: Int,

    @field:Schema(description = "총 소요 시간 (초)", example = "300")
    val elapsedSeconds: Int,

    @field:Schema(description = "힌트 사용 횟수", example = "2")
    val hintUsageCount: Int,

    @field:Schema(description = "오답 제출 횟수", example = "1")
    val wrongSubmissionCount: Int,

    @field:Schema(description = "풀었던 단어들의 학습 정보")
    val words: List<WordDetailResponse>
)

@Schema(description = "단어 학습 정보 응답")
data class WordDetailResponse(
    @field:Schema(description = "단어 원형", example = "창조")
    val surfaceForm: String,

    @field:Schema(description = "사전적 정의", example = "하나님이 세상을 만드신 행위")
    val dictionaryDefinition: String,

    @field:Schema(description = "원어 코드 (HEBREW / GREEK). 원어 정보가 없는 사전 항목은 null", example = "HEBREW")
    val originalLanguageCode: String?,

    @field:Schema(description = "원어 어휘. 원어 정보가 없는 사전 항목은 null", example = "בָּרָא")
    val originalLexeme: String?,

    @field:Schema(description = "성경 구절 참조 목록")
    val references: List<WordReferenceResponse>
)

@Schema(description = "성경 구절 참조 응답")
data class WordReferenceResponse(
    @field:Schema(description = "구절 참조", example = "창세기 1:1")
    val verseReference: String,

    @field:Schema(description = "구절 발췌", example = "태초에 하나님이 천지를 창조하시니라")
    val verseExcerpt: String
)

@Schema(description = "전체 제출 오답 응답")
data class SubmitWrongResponse(
    @field:Schema(description = "결과", example = "WRONG")
    val result: String = "WRONG",

    @field:Schema(description = "틀린 셀 좌표 목록")
    val wrongCells: List<WrongCellResponse>,

    @field:Schema(description = "누적 오답 제출 횟수", example = "2")
    val wrongSubmissionCount: Int,

    @field:Schema(description = "서버에 저장된 누적 경과 시간 (초)", example = "300")
    val elapsedSeconds: Int
)

@Schema(description = "틀린 셀 좌표")
data class WrongCellResponse(
    @field:Schema(description = "행 인덱스", example = "2")
    val row: Int,

    @field:Schema(description = "열 인덱스", example = "5")
    val col: Int
)
