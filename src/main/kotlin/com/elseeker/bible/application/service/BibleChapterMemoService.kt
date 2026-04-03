package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleChapterMemoRepository
import com.elseeker.bible.domain.model.BibleChapterMemo
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class BibleChapterMemoService(
    private val bibleChapterMemoRepository: BibleChapterMemoRepository
) {

    @Transactional(readOnly = true)
    fun getChapterMemo(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleChapterMemo? =
        bibleChapterMemoRepository.findByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
            memberUid,
            translationId,
            bookOrder,
            chapterNumber
        )

    fun upsertChapterMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        content: String
    ): BibleChapterMemo {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "content")
        }
        val existing = bibleChapterMemoRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber
        )
        if (existing != null) {
            existing.updateContent(trimmed)
            return existing
        }
        return bibleChapterMemoRepository.save(
            BibleChapterMemo(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                content = trimmed
            )
        )
    }

    fun deleteChapterMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ) {
        val existing = bibleChapterMemoRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber
        ) ?: return
        bibleChapterMemoRepository.delete(existing)
    }
}
