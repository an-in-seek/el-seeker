package com.elseeker.common

import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.vo.MemberRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(resolver = TestProfileResolver::class)   // 변경하지 말 것
abstract class IntegrationTest : TestContainers() {

    // ========== 공용 의존성 ==========
    @Autowired
    protected lateinit var databaseCleaner: DatabaseCleaner

    @Autowired
    private lateinit var memberRepository: MemberRepository

    protected lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = memberRepository.save(
            Member.create(
                email = "seek@elseeker.com",
                nickname = "seek",
                profileImageUrl = null,
                memberRole = MemberRole.USER
            )
        )
    }

    @AfterEach
    fun tearDown() {
        databaseCleaner.execute()
    }

}