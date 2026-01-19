package com.elseeker.game.application.service

import com.elseeker.game.adapter.input.api.response.BibleTypingVerseProgressResponse
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.output.jpa.BibleTypingVerseProgressRepository
import com.elseeker.game.domain.model.BibleTypingVerseProgress
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BibleTypingVerseProgressService(
    private val bibleTypingVerseProgressRepository: BibleTypingVerseProgressRepository
) {

    @Transactional
    fun saveProgress(member: Member, request: BibleTypingVerseProgressRequest): BibleTypingVerseProgress {
        val existing = bibleTypingVerseProgressRepository.findFirstByMemberAndSessionKeyAndVerseNumber(
            member,
            request.sessionKey,
            request.verseNumber
        )
        if (existing != null) {
            existing.originalText = request.originalText
            existing.typedText = request.typedText
            existing.accuracy = request.accuracy
            existing.cpm = request.cpm
            existing.elapsedSeconds = request.elapsedSeconds
            existing.completed = request.completed
            return bibleTypingVerseProgressRepository.save(existing)
        }
        val progress = BibleTypingVerseProgress(
            member = member,
            sessionKey = request.sessionKey,
            translationId = request.translationId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            verseNumber = request.verseNumber,
            originalText = request.originalText,
            typedText = request.typedText,
            accuracy = request.accuracy,
            cpm = request.cpm,
            elapsedSeconds = request.elapsedSeconds,
            completed = request.completed
        )
        return bibleTypingVerseProgressRepository.save(progress)
    }

    @Transactional(readOnly = true)
    fun getLatestProgress(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleTypingVerseProgressResponse? {
        val latest = bibleTypingVerseProgressRepository
            .findTopByMemberAndTranslationIdAndBookOrderAndChapterNumberOrderByCreatedAtDesc(
                member,
                translationId,
                bookOrder,
                chapterNumber
            ) ?: return null

        return buildProgressResponse(member, latest)
    }

    @Transactional(readOnly = true)
    fun getLatestProgress(member: Member): BibleTypingVerseProgressResponse? {
        val latest = bibleTypingVerseProgressRepository
            .findTopByMemberOrderByCreatedAtDesc(member)
            ?: return null
        return buildProgressResponse(member, latest)
    }

    private fun buildProgressResponse(
        member: Member,
        latest: BibleTypingVerseProgress
    ): BibleTypingVerseProgressResponse {
        val verses = bibleTypingVerseProgressRepository
            .findAllByMemberAndSessionKeyOrderByVerseNumberAsc(member, latest.sessionKey)
            .map {
                BibleTypingVerseProgressResponse.VerseProgress(
                    verseNumber = it.verseNumber,
                    typedText = it.typedText,
                    accuracy = it.accuracy,
                    cpm = it.cpm,
                    elapsedSeconds = it.elapsedSeconds,
                    completed = it.completed,
                    createdAt = it.createdAt
                )
            }

        return BibleTypingVerseProgressResponse(
            sessionKey = latest.sessionKey,
            translationId = latest.translationId,
            bookOrder = latest.bookOrder,
            chapterNumber = latest.chapterNumber,
            createdAt = latest.createdAt,
            verses = verses
        )
    }
}
