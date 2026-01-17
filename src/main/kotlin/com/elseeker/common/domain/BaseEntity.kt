package com.elseeker.common.domain

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable

@MappedSuperclass // 1. 이 클래스는 테이블로 생성되지 않고, 상속받는 엔티티에 매핑 정보만 제공함
@EntityListeners(AuditingEntityListener::class) // Auditing 기능 포함 (선택 사항)
abstract class BaseEntity : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    // 2. 공통 equals 구현
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BaseEntity
        // id가 없으면(비영속) 동등하지 않음
        return id != null && id == other.id
    }

    // 3. 공통 hashCode 구현
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}