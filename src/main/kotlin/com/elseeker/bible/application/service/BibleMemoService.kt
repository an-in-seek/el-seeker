package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
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
    private val bibleBookRepository: BibleBookRepository,
    private val bibleTranslationRepository: BibleTranslationRepository
) {

    @Transactional(readOnly = true)
    fun getMyMemos(
        memberUid: UUID,
        pageable: Pageable,
        translationId: Long? = null,
        bookOrder: Int? = null
    ): BibleMemoResult.MemoSlice {
        val sortedPageable = if (pageable.sort.isUnsorted) {
            org.springframework.data.domain.PageRequest.of(
                pageable.pageNumber,
                pageable.pageSize,
                Sort.by(Sort.Direction.DESC, "updatedAt")
            )
        } else {
            pageable
        }
        val slice = when {
            translationId != null && bookOrder != null ->
                bibleMemoRepository.findMemoItemsByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder, sortedPageable)
            translationId != null ->
                bibleMemoRepository.findMemoItemsByMemberUidAndTranslationId(memberUid, translationId, sortedPageable)
            bookOrder != null ->
                bibleMemoRepository.findMemoItemsByMemberUidAndBookOrder(memberUid, bookOrder, sortedPageable)
            else ->
                bibleMemoRepository.findMemoItemsByMemberUid(memberUid, sortedPageable)
        }

        val totalCount: Long? = when {
            sortedPageable.pageNumber != 0 -> null
            // 단일 페이지로 끝나면 count 쿼리 생략 — slice 크기가 곧 총 개수
            !slice.hasNext() -> slice.content.size.toLong()
            translationId != null && bookOrder != null ->
                bibleMemoRepository.countByMemberUidAndTranslationIdAndBookOrder(memberUid, translationId, bookOrder)
            translationId != null ->
                bibleMemoRepository.countByMemberUidAndTranslationId(memberUid, translationId)
            bookOrder != null ->
                bibleMemoRepository.countByMemberUidAndBookOrder(memberUid, bookOrder)
            else ->
                bibleMemoRepository.countByMemberUid(memberUid)
        }

        return BibleMemoResult.MemoSlice(
            content = slice.content.map { memo ->
                BibleMemoResult.MemoItem(
                    memoId = memo.memoId,
                    translationId = memo.translationId,
                    bookOrder = memo.bookOrder,
                    bookName = memo.bookName,
                    chapterNumber = memo.chapterNumber,
                    verseNumber = memo.verseNumber,
                    content = memo.content,
                    updatedAt = memo.updatedAt
                )
            },
            hasNext = slice.hasNext(),
            size = slice.size,
            number = slice.number,
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun getMemoTranslations(memberUid: UUID): List<BibleMemoResult.MemoTranslationItem> {
        val translationIds = bibleMemoRepository.findDistinctTranslationIdsByMemberUid(memberUid)
        if (translationIds.isEmpty()) return emptyList()
        val translations = bibleTranslationRepository.findAllById(translationIds)
        return translations
            .sortedBy { it.translationOrder }
            .map { BibleMemoResult.MemoTranslationItem(translationId = it.id!!, translationName = it.name) }
    }

    @Transactional(readOnly = true)
    fun getMemoBookList(memberUid: UUID, translationId: Long): List<BibleMemoResult.MemoBookItem> {
        val bookOrders = bibleMemoRepository.findDistinctBookOrdersByMemberUidAndTranslationId(memberUid, translationId)
        if (bookOrders.isEmpty()) return emptyList()
        val books = bibleBookRepository.findByTranslationIdAndBookOrderIn(translationId, bookOrders)
        return books
            .sortedBy { it.bookOrder }
            .map { BibleMemoResult.MemoBookItem(bookOrder = it.bookOrder, bookName = it.name) }
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
