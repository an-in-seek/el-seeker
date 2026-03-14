package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
import com.elseeker.bible.domain.model.BibleVerseMemo
import com.elseeker.bible.domain.result.BibleMemoResult
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class BibleMemoService(
    private val bibleMemoRepository: BibleMemoRepository,
    private val bibleBookRepository: BibleBookRepository
) {

    @Transactional(readOnly = true)
    fun getMyMemos(memberUid: UUID, pageable: Pageable): BibleMemoResult.MemoSlice {
        val sortedPageable = if (pageable.sort.isUnsorted) {
            org.springframework.data.domain.PageRequest.of(
                pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")
            )
        } else {
            pageable
        }
        val slice = bibleMemoRepository.findAllByMemberUid(memberUid, sortedPageable)

        val bookNameMap = resolveBookNames(slice.content)

        val totalCount = if (sortedPageable.pageNumber == 0) {
            bibleMemoRepository.countByMemberUid(memberUid)
        } else {
            null
        }

        return BibleMemoResult.MemoSlice(
            content = slice.content.map { memo ->
                val bookName = bookNameMap[memo.translationId to memo.bookOrder] ?: ""
                BibleMemoResult.MemoItem.from(memo, bookName)
            },
            hasNext = slice.hasNext(),
            size = slice.size,
            number = slice.number,
            totalCount = totalCount
        )
    }

    private fun resolveBookNames(memos: List<BibleVerseMemo>): Map<Pair<Long, Int>, String> {
        val keys = memos.map { it.translationId to it.bookOrder }.distinct()
        return keys.mapNotNull { (translationId, bookOrder) ->
            bibleBookRepository.findByTranslationAndBook(translationId, bookOrder)
                ?.let { (translationId to bookOrder) to it.name }
        }.toMap()
    }

    @Transactional(readOnly = true)
    fun getChapterMemos(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseMemo> =
        bibleMemoRepository.findAllByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
            memberUid,
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
