package com.elseeker.common.security.oauth.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.common.security.oauth.factory.OAuth2UserInfoFactory
import com.elseeker.member.adapter.output.jpa.MemberOAuthAccountRepository
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.vo.MemberRole
import com.elseeker.member.domain.vo.OAuthProvider
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository,
    private val memberOAuthAccountRepository: MemberOAuthAccountRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        // 1. Factory를 통해 Provider별 파싱된 정보 획득
        val provider = OAuthProvider.fromRegistrationId(userRequest.clientRegistration.registrationId)
        val userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.attributes)

        // 2. 이메일 검증 (필수 값인 경우)
        if (userInfo.email.isBlank()) {
            throwError(ErrorType.OAUTH_EMAIL_MISSING, provider.registrationId)
        }
        if (userInfo.providerUserId.isBlank()) {
            throwError(ErrorType.OAUTH_PROVIDER_USER_ID_MISSING, provider.registrationId)
        }

        // 3. 사용자 저장 또는 업데이트 (OAuth 계정 기준)
        val oauthAccount = memberOAuthAccountRepository.findByProviderAndProviderUserId(
            provider = userInfo.provider,
            providerUserId = userInfo.providerUserId
        )
        val member = oauthAccount?.let { account ->
            account.syncOAuthProfile(
                email = userInfo.email,
                nickname = userInfo.name,
                profileImageUrl = userInfo.imageUrl
            )
            account.member
        }
            ?: Member.create(
                email = userInfo.email,
                nickname = "",
                memberRole = MemberRole.USER,
                profileImageUrl = null
            ).also { newMember ->
                newMember.addOAuthAccount(
                    provider = userInfo.provider,
                    providerUserId = userInfo.providerUserId,
                    email = userInfo.email,
                    oauthNickname = userInfo.name,
                    oauthProfileImageUrl = userInfo.imageUrl
                )
            }
        val savedMember = memberRepository.save(member)
        val savedMemberUid = savedMember.uid

        // 4. 기존 attributes에 내부 시스템용 데이터(memberUid, role) 추가
        // 주의: Handler에서 attributes["memberUid"] 등으로 접근하므로 반드시 포함시켜야 함
        val enrichedAttributes = HashMap<String, Any>(oAuth2User.attributes)
        enrichedAttributes["memberUid"] = savedMemberUid.toString()
        enrichedAttributes["role"] = savedMember.memberRole.name
        enrichedAttributes["email"] = savedMember.email // Provider 구조에 따라 최상위에 없을 수 있으므로 명시적 추가

        // 5. authorities
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${savedMember.memberRole.name}"))

        // 6. UserInfoEndpoint의 userNameAttributeName 가져오기
        // (Google은 "sub", Naver는 "response", Kakao는 "id" 등이 될 수 있음)
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        return DefaultOAuth2User(authorities, enrichedAttributes, userNameAttributeName)
    }
}
