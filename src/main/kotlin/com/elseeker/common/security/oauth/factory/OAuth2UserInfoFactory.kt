package com.elseeker.common.security.oauth.factory

import com.elseeker.common.security.oauth.info.GoogleOAuth2UserInfo
import com.elseeker.common.security.oauth.info.KakaoOAuth2UserInfo
import com.elseeker.common.security.oauth.info.NaverOAuth2UserInfo
import com.elseeker.common.security.oauth.info.OAuth2UserInfo
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

object OAuth2UserInfoFactory {

    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "naver" -> NaverOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자입니다: $registrationId")
        }
    }

}