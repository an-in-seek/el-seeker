package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleChapterRepository
import com.elseeker.bible.domain.model.BibleChapter
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminBibleChapterService(
    private val chapterRepository: BibleChapterRepository,
) {
    fun findByBookId(bookId: Long, pageable: Pageable): Page<BibleChapter> =
        chapterRepository.findByBookId(bookId, pageable)

    fun findById(id: Long): BibleChapter =
        chapterRepository.findByIdOrNull(id) ?: throwError(ErrorType.CHAPTER_NOT_FOUND, "id=$id")

    @Transactional
    fun create(bookId: Long, chapterNumber: Int): BibleChapter =
        chapterRepository.save(BibleChapter(bookId = bookId, chapterNumber = chapterNumber))

    @Transactional
    fun update(id: Long, chapterNumber: Int): BibleChapter {
        val existing = findById(id)
        val updated = BibleChapter(id = existing.id, bookId = existing.bookId, chapterNumber = chapterNumber)
        return chapterRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        chapterRepository.delete(entity)
    }
}
