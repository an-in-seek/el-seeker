package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
import com.elseeker.bible.domain.model.BibleBookMemo
import com.elseeker.bible.domain.result.BibleBookMemoResult
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
class BibleBookMemoService(
    private val bibleBookMemoRepository: BibleBookMemoRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleTranslationRepository: BibleTranslationRepository
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

    @Transactional(readOnly = true)
    fun getMyBookMemos(
        memberUid: UUID,
        pageable: Pageable,
        translationId: Long? = null,
        bookOrder: Int? = null
    ): BibleBookMemoResult.BookMemoSlice {
        val sortedPageable = if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        } else {
            pageable
        }
        val slice = when {
            translationId != null && bookOrder != null ->
                bibleBookMemoRepository.findAllByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder, sortedPageable)
            translationId != null ->
                bibleBookMemoRepository.findAllByMemberUidAndTranslationId(memberUid, translationId, sortedPageable)
            else ->
                bibleBookMemoRepository.findAllByMemberUid(memberUid, sortedPageable)
        }

        val bookNameMap = resolveBookNames(slice.content)

        val totalCount = if (sortedPageable.pageNumber == 0) {
            when {
                translationId != null && bookOrder != null ->
                    bibleBookMemoRepository.countByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder)
                translationId != null ->
                    bibleBookMemoRepository.countByMemberUidAndTranslationId(memberUid, translationId)
                else ->
                    bibleBookMemoRepository.countByMemberUid(memberUid)
            }
        } else {
            null
        }

        return BibleBookMemoResult.BookMemoSlice(
            content = slice.content.map { memo ->
                val bookName = bookNameMap[memo.translationId to memo.bookOrder] ?: ""
                BibleBookMemoResult.BookMemoItem.from(memo, bookName)
            },
            hasNext = slice.hasNext(),
            size = slice.size,
            number = slice.number,
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getMemoTranslations(memberUid: UUID): List<BibleMemoResult.MemoTranslationItem> {
        val translationIds = bibleBookMemoRepository.findDistinctTranslationIdsByMemberUid(memberUid)
        if (translationIds.isEmpty()) return emptyList()
        val translations = bibleTranslationRepository.findAllById(translationIds)
        return translations
            .sortedBy { it.translationOrder }
            .map { BibleMemoResult.MemoTranslationItem(translationId = it.id!!, translationName = it.name) }
    }

    @Transactional(readOnly = true)
    fun getMemoBookList(memberUid: UUID, translationId: Long): List<BibleMemoResult.MemoBookItem> {
        val bookOrders = bibleBookMemoRepository.findDistinctBookOrdersByMemberUidAndTranslationId(memberUid, translationId)
        if (bookOrders.isEmpty()) return emptyList()
        val books = bibleBookRepository.findByTranslationIdAndBookOrderIn(translationId, bookOrders)
        return books
            .sortedBy { it.bookOrder }
            .map { BibleMemoResult.MemoBookItem(bookOrder = it.bookOrder, bookName = it.name) }
    }

    private fun resolveBookNames(memos: List<BibleBookMemo>): Map<Pair<Long, Int>, String> {
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
