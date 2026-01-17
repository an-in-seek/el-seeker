package com.elseeker.common.security.oauth.info

import com.elseeker.member.domain.vo.OAuthProvider

// Google 구현체
class GoogleOAuth2UserInfo(
    override val attributes: Map<String, Any>
) : OAuth2UserInfo {

    override val providerUserId: String
        get() = attributes["sub"] as String

    override val provider: OAuthProvider
        get() = OAuthProvider.GOOGLE

    override val email: String
        get() = attributes["email"] as String

    override val name: String
        get() = attributes["name"] as String

    override val imageUrl: String?
        get() = attributes["picture"] as? String
}
