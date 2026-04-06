package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.request.BibleBookMemoRequest
import com.elseeker.bible.adapter.input.api.client.response.BibleBookMemoApiResponse
import com.elseeker.bible.application.service.BibleBookMemoService
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import com.elseeker.member.domain.model.Member
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}")
class BibleBookMemoApi(
    private val bibleBookMemoService: BibleBookMemoService,
    private val memberService: MemberService
) : BibleBookMemoApiDocument {

    @GetMapping("/book-memo")
    override fun getBookMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleBookMemoApiResponse.BookMemoItem> {
        val memo = bibleBookMemoService.getBookMemo(
            principal.memberUid,
            translationId,
            bookOrder
        ) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(BibleBookMemoApiResponse.BookMemoItem.from(memo))
    }

    @PutMapping("/book-memo")
    override fun upsertBookMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleBookMemoRequest
    ): ResponseEntity<BibleBookMemoApiResponse.BookMemoItem> {
        val member = getMember(principal)
        val memo = bibleBookMemoService.upsertBookMemo(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            content = request.content
        )
        return ResponseEntity.ok(BibleBookMemoApiResponse.BookMemoItem.from(memo))
    }

    @DeleteMapping("/book-memo")
    override fun deleteBookMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        val member = getMember(principal)
        bibleBookMemoService.deleteBookMemo(
            member,
            translationId,
            bookOrder
        )
        return ResponseEntity.noContent().build()
    }

    private fun getMember(principal: JwtPrincipal): Member =
        memberService.getMember(principal.memberUid)
}
