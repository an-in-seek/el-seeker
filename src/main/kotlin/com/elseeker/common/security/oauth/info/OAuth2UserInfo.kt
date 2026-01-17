package com.elseeker.common.security.oauth.info

// 1. 공통 인터페이스
interface OAuth2UserInfo {
    val attributes: Map<String, Any>
    val providerUserId: String
    val provider: String
    val email: String
    val name: String
    val imageUrl: String?
}