package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleHighlightRepository
import com.elseeker.bible.domain.model.BibleHighlightColor
import com.elseeker.bible.domain.model.BibleVerseHighlight
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BibleHighlightService(
    private val bibleHighlightRepository: BibleHighlightRepository
) {

    @Transactional(readOnly = true)
    fun getChapterHighlights(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseHighlight> =
        bibleHighlightRepository.findAllByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber
        )

    fun upsertHighlight(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int,
        colorValue: String
    ): BibleVerseHighlight {
        val color = runCatching { BibleHighlightColor.from(colorValue) }
            .getOrElse { throwError(ErrorType.INVALID_PARAMETER, "color") }
        val existing = bibleHighlightRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber,
            verseNumber
        )
        if (existing != null) {
            existing.updateColor(color)
            return existing
        }
        return bibleHighlightRepository.save(
            BibleVerseHighlight(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                verseNumber = verseNumber,
                color = color
            )
        )
    }

    fun deleteHighlight(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ) {
        val existing = bibleHighlightRepository.findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
            member,
            translationId,
            bookOrder,
            chapterNumber,
            verseNumber
        ) ?: return
        bibleHighlightRepository.delete(existing)
    }
}
