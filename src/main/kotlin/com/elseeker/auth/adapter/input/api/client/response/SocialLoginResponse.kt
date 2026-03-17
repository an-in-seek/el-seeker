package com.elseeker.auth.adapter.input.api.client.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 모바일 소셜 로그인 응답 DTO.
 *
 * 앱에서 토큰을 안전하게 저장한 뒤, 이후 API 호출 시
 * Authorization: Bearer {accessToken} 헤더로 전달합니다.
 */
data class SocialLoginResponse(

    @Schema(description = "JWT Access Token")
    val accessToken: String,

    @Schema(description = "JWT Refresh Token")
    val refreshToken: String,
)
