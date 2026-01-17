package com.elseeker.member.domain.vo

enum class MemberRole(
    val key: String,       // Spring Security에서 사용할 권한 키 (접두사 ROLE_ 포함)
    val title: String      // UI(프론트엔드/관리자페이지)에 보여줄 직관적인 명칭
) {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    companion object {
        // 문자열을 Enum으로 변환하는 유틸리티 메서드 (필요시 사용)
        fun fromKey(key: String): MemberRole? {
            return entries.find { it.key == key }
        }
    }
}