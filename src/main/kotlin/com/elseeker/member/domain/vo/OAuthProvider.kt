package com.elseeker.member.domain.vo

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class OAuthProvider(val registrationId: String) {
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    companion object {
        fun fromRegistrationId(registrationId: String): OAuthProvider {
            val normalized = registrationId.lowercase()
            return values().firstOrNull { it.registrationId == normalized }
                ?: throw IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: $registrationId")
        }
    }
}

@Converter
class OAuthProviderConverter : AttributeConverter<OAuthProvider, String> {
    override fun convertToDatabaseColumn(attribute: OAuthProvider?): String? {
        return attribute?.registrationId
    }

    override fun convertToEntityAttribute(dbData: String?): OAuthProvider? {
        return dbData?.let { OAuthProvider.fromRegistrationId(it) }
    }
}
