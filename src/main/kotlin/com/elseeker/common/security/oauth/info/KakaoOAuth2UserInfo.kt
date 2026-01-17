package com.elseeker.common.security.oauth.info

// Kakao 구현체 (카카오는 구조가 다소 복잡함: id는 최상위, 정보는 kakao_account 내부에 존재)
class KakaoOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo {
    private val kakaoAccount: Map<String, Any> by lazy {
        attributes["kakao_account"] as? Map<String, Any> ?: emptyMap()
    }

    private val profile: Map<String, Any> by lazy {
        kakaoAccount["profile"] as? Map<String, Any> ?: emptyMap()
    }

    override val providerUserId: String
        get() = attributes["id"].toString()

    override val provider: String
        get() = "kakao"

    override val email: String
        get() = kakaoAccount["email"] as? String ?: "" // 카카오는 이메일 제공 동의가 선택일 수 있음

    override val name: String
        get() = profile["nickname"] as? String ?: "Unknown"

    override val imageUrl: String?
        get() = profile["profile_image_url"] as? String
}