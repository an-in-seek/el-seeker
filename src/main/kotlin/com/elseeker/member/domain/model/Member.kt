package com.elseeker.member.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.vo.MemberRole
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(
    name = "member",
    // 1. 조회 성능을 위해 이메일에 인덱스 적용
    indexes = [Index(name = "idx_member_email", columnList = "email")]
)
@EntityListeners(AuditingEntityListener::class) // 2. 생성/수정 시간 자동 추적 활성화
class Member(

    @Column(nullable = false, length = 255, unique = true)
    var email: String,

    // 3. 'name'은 DB 예약어 충돌 위험과 의미 모호함이 있어 'nickname'으로 변경
    @Column(nullable = false, length = 100)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var memberRole: MemberRole = MemberRole.USER,

    // 소셜 플랫폼 이름 (google, naver, kakao)
    @Column(nullable = false, length = 50)
    var provider: String,

    // 공급자가 부여한 사용자 ID
    @Column(nullable = false, length = 255)
    var providerUserId: String

) : BaseTimeEntity() {


}