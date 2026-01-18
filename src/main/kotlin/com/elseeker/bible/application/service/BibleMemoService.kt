package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
import com.elseeker.bible.domain.model.BibleVerseMemo
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BibleMemoService(
    private val bibleMemoRepository: BibleMemoRepository
) {

    @Transactional(readOnly = true)
    fun getChapterMemos(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseMemo> =
        bibleMemoRepository.findAllByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber
        )

    fun upsertMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int,
        content: String
    ): BibleVerseMemo {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "content")
        }
        val existing = bibleMemoRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber,
            verseNumber
        )
        if (existing != null) {
            existing.updateContent(trimmed)
            return existing
        }
        return bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                verseNumber = verseNumber,
                content = trimmed
            )
        )
    }

    fun deleteMemo(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ) {
        val existing = bibleMemoRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber,
            verseNumber
        ) ?: return
        bibleMemoRepository.delete(existing)
    }
}
