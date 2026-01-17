package com.elseeker.common.security.oauth.info

// Naver 구현체 (네이버는 'response'라는 키 안에 실제 정보가 담겨 있음)
class NaverOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo {
    private val response: Map<String, Any> by lazy {
        attributes["response"] as? Map<String, Any> ?: emptyMap()
    }

    override val providerUserId: String
        get() = response["id"] as String

    override val provider: String
        get() = "naver"

    override val email: String
        get() = response["email"] as String

    override val name: String
        get() = response["name"] as String

    override val imageUrl: String?
        get() = response["profile_image"] as? String
}