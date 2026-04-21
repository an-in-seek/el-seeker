package com.elseeker.bible.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 3 탭(책/장/절)의 메모 총 개수를 한 번의 round-trip 으로 반환.
 * 프론트의 탭 배지 프리페치용.
 */
@RestController
@RequestMapping("/api/v1/bibles/my-memo-counts")
class BibleMyMemoCountsApi(
    private val memberRepository: MemberRepository,
) {

    @GetMapping
    fun getMyMemoCounts(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<MemoCountsResponse> {
        val counts = memberRepository.findMemoCountsByUid(principal.memberUid)
        return ResponseEntity.ok(
            MemoCountsResponse(
                book = counts?.book ?: 0,
                chapter = counts?.chapter ?: 0,
                verse = counts?.verse ?: 0,
            )
        )
    }

    data class MemoCountsResponse(
        val book: Long,
        val chapter: Long,
        val verse: Long,
    )
}
