package com.elseeker.common.security.oauth.service

import com.elseeker.common.security.oauth.factory.OAuth2UserInfoFactory
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.vo.MemberRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        // 1. Factory를 통해 Provider별 파싱된 정보 획득
        val provider = userRequest.clientRegistration.registrationId
        val userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.attributes)

        // 2. 이메일 검증 (필수 값인 경우)
        if (userInfo.email.isBlank()) {
            throw IllegalArgumentException("$provider 로그인에서 이메일 정보를 찾을 수 없습니다.")
        }

        // 3. 사용자 저장 또는 업데이트
        val savedUser = memberRepository.findByEmail(userInfo.email)?.apply {
            this.nickname = userInfo.name
            this.provider = userInfo.provider
            this.providerUserId = userInfo.providerUserId
            // 필요 시 프로필 이미지 업데이트 로직 추가
        } ?: Member(
            email = userInfo.email,
            nickname = userInfo.name,
            memberRole = MemberRole.USER,
            provider = userInfo.provider,
            providerUserId = userInfo.providerUserId
        )

        val user = memberRepository.save(savedUser)

        // 4. 기존 attributes에 내부 시스템용 데이터(userId, role) 추가
        // 주의: Handler에서 attributes["userId"] 등으로 접근하므로 반드시 포함시켜야 함
        val enrichedAttributes = HashMap<String, Any>(oAuth2User.attributes)
        enrichedAttributes["userId"] = requireNotNull(user.id) { "User ID must not be null after save" }
        enrichedAttributes["role"] = user.memberRole.name
        enrichedAttributes["email"] = user.email // Provider 구조에 따라 최상위에 없을 수 있으므로 명시적 추가

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.memberRole.name}"))

        // 5. UserInfoEndpoint의 userNameAttributeName 가져오기
        // (Google은 "sub", Naver는 "response", Kakao는 "id" 등이 될 수 있음)
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        return DefaultOAuth2User(authorities, enrichedAttributes, userNameAttributeName)
    }
}
