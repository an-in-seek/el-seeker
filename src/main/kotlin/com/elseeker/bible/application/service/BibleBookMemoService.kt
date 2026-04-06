package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookMemoRepository
import com.elseeker.bible.domain.model.BibleBookMemo
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class BibleBookMemoService(
    private val bibleBookMemoRepository: BibleBookMemoRepository
) {

    @Transactional(readOnly = true)
    fun getBookMemo(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int
    ): BibleBookMemo? =
        bibleBookMemoRepository.findByMemberUidAndTranslationIdAndBookOrder(
            memberUid,
            translationId,
            bookOrder
        )

    fun upsertBookMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        content: String
    ): BibleBookMemo {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "content")
        }
        val existing = bibleBookMemoRepository.findByMemberAndTranslationIdAndBookOrder(
            member,
            translationId,
            bookOrder
        )
        if (existing != null) {
            existing.updateContent(trimmed)
            return existing
        }
        return bibleBookMemoRepository.save(
            BibleBookMemo(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                content = trimmed
            )
        )
    }

    fun deleteBookMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int
    ) {
        val existing = bibleBookMemoRepository.findByMemberAndTranslationIdAndBookOrder(
            member,
            translationId,
            bookOrder
        ) ?: return
        bibleBookMemoRepository.delete(existing)
    }
}
