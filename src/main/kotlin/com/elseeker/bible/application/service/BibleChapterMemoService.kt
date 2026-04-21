package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleChapterMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
import com.elseeker.bible.domain.model.BibleChapterMemo
import com.elseeker.bible.domain.result.BibleChapterMemoResult
import com.elseeker.bible.domain.result.BibleMemoResult
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class BibleChapterMemoService(
    private val bibleChapterMemoRepository: BibleChapterMemoRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleTranslationRepository: BibleTranslationRepository
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

    @Transactional(readOnly = true)
    fun getMyChapterMemos(
        memberUid: UUID,
        pageable: Pageable,
        translationId: Long? = null,
        bookOrder: Int? = null
    ): BibleChapterMemoResult.ChapterMemoSlice {
        val sortedPageable = if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        } else {
            pageable
        }
        val slice = when {
            translationId != null && bookOrder != null ->
                bibleChapterMemoRepository.findAllByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder, sortedPageable)
            translationId != null ->
                bibleChapterMemoRepository.findAllByMemberUidAndTranslationId(memberUid, translationId, sortedPageable)
            else ->
                bibleChapterMemoRepository.findAllByMemberUid(memberUid, sortedPageable)
        }

        val bookNameMap = resolveBookNames(slice.content)

        val totalCount = if (sortedPageable.pageNumber == 0) {
            when {
                translationId != null && bookOrder != null ->
                    bibleChapterMemoRepository.countByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder)
                translationId != null ->
                    bibleChapterMemoRepository.countByMemberUidAndTranslationId(memberUid, translationId)
                else ->
                    bibleChapterMemoRepository.countByMemberUid(memberUid)
            }
        } else {
            null
        }

        return BibleChapterMemoResult.ChapterMemoSlice(
            content = slice.content.map { memo ->
                val bookName = bookNameMap[memo.translationId to memo.bookOrder] ?: ""
                BibleChapterMemoResult.ChapterMemoItem.from(memo, bookName)
            },
            hasNext = slice.hasNext(),
            size = slice.size,
            number = slice.number,
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getMemoTranslations(memberUid: UUID): List<BibleMemoResult.MemoTranslationItem> {
        val translationIds = bibleChapterMemoRepository.findDistinctTranslationIdsByMemberUid(memberUid)
        if (translationIds.isEmpty()) return emptyList()
        val translations = bibleTranslationRepository.findAllById(translationIds)
        return translations
            .sortedBy { it.translationOrder }
            .map { BibleMemoResult.MemoTranslationItem(translationId = it.id!!, translationName = it.name) }
    }

    @Transactional(readOnly = true)
    fun getMemoBookList(memberUid: UUID, translationId: Long): List<BibleMemoResult.MemoBookItem> {
        val bookOrders = bibleChapterMemoRepository.findDistinctBookOrdersByMemberUidAndTranslationId(memberUid, translationId)
        if (bookOrders.isEmpty()) return emptyList()
        val books = bibleBookRepository.findByTranslationIdAndBookOrderIn(translationId, bookOrders)
        return books
            .sortedBy { it.bookOrder }
            .map { BibleMemoResult.MemoBookItem(bookOrder = it.bookOrder, bookName = it.name) }
    }

    private fun resolveBookNames(memos: List<BibleChapterMemo>): Map<Pair<Long, Int>, String> {
        return memos
            .map { it.translationId to it.bookOrder }
            .distinct()
            .groupBy({ it.first }, { it.second })
            .flatMap { (translationId, bookOrders) ->
                bibleBookRepository.findByTranslationIdAndBookOrderIn(translationId, bookOrders)
                    .map { (translationId to it.bookOrder) to it.name }
            }
            .toMap()
    }

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
