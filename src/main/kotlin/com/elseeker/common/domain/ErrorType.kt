package com.elseeker.common.domain

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(
    val status: HttpStatus,
    val message: String,
    val logLevel: LogLevel
) {
    // 400
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청 파라미터입니다.", LogLevel.INFO),
    PROVIDER_MISMATCH(HttpStatus.BAD_REQUEST, "해당 계정은 이미 가입되어 있습니다.", LogLevel.INFO),
    PROVIDER_USER_ID_MISMATCH(HttpStatus.BAD_REQUEST, "이미 가입된 계정과 사용자 식별자가 일치하지 않습니다.", LogLevel.INFO),
    OAUTH_EMAIL_MISSING(HttpStatus.BAD_REQUEST, "소셜 로그인 이메일 정보를 찾을 수 없습니다.", LogLevel.INFO),
    OAUTH_PROVIDER_USER_ID_MISSING(HttpStatus.BAD_REQUEST, "소셜 로그인 사용자 식별 정보를 찾을 수 없습니다.", LogLevel.INFO),
    MEMBER_ID_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 식별 정보가 없습니다.", LogLevel.ERROR),

    // 404
    TRANSLATION_NOT_FOUND(HttpStatus.NOT_FOUND, "번역본을 찾을 수 없습니다.", LogLevel.WARN),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "성경을 찾을 수 없습니다.", LogLevel.WARN),
    BOOK_DESCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "성경 개요를 찾을 수 없습니다.", LogLevel.WARN),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "장을 찾을 수 없습니다.", LogLevel.WARN),
    DICTIONARY_NOT_FOUND(HttpStatus.NOT_FOUND, "성경 사전 용어를 찾을 수 없습니다.", LogLevel.WARN),
    QUIZ_STAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈 스테이지를 찾을 수 없습니다.", LogLevel.WARN),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.", LogLevel.WARN),

    // 403
    MEMBER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "요청한 회원 정보에 접근할 수 없습니다.", LogLevel.WARN),

    // 500
    SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "검색 처리 중 오류가 발생했습니다.", LogLevel.ERROR),
    DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.", LogLevel.ERROR),
    UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.", LogLevel.ERROR)
}
