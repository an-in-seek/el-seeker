package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleReadingProgressRepository
import com.elseeker.bible.domain.model.BibleReadingProgress
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class BibleReadingProgressService(
    private val bibleReadingProgressRepository: BibleReadingProgressRepository
) {

    @Transactional(readOnly = true)
    fun getReadChapters(member: Member, translationId: Long, bookOrder: Int): List<Int> =
        bibleReadingProgressRepository.findAllByMemberAndTranslationIdAndBookOrder(
            member, translationId, bookOrder
        ).map { it.chapterNumber }

    @Transactional(readOnly = true)
    fun isChapterRead(member: Member, translationId: Long, bookOrder: Int, chapterNumber: Int): Boolean =
        bibleReadingProgressRepository.existsByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member, translationId, bookOrder, chapterNumber
        )

    fun markChapterAsRead(member: Member, translationId: Long, bookOrder: Int, chapterNumber: Int) {
        val alreadyRead = bibleReadingProgressRepository.existsByMemberAndTranslationIdAndBookOrderAndChapterNumber(
            member, translationId, bookOrder, chapterNumber
        )
        if (alreadyRead) {
            return
        }
        bibleReadingProgressRepository.save(
            BibleReadingProgress(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                readAt = Instant.now()
            )
        )
    }
}
