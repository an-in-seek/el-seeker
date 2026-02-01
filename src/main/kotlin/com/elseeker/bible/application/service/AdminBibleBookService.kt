package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.domain.model.BibleBook
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminBibleBookService(
    private val bookRepository: BibleBookRepository,
) {
    fun findByTranslationId(translationId: Long, pageable: Pageable): Page<BibleBook> =
        bookRepository.findByTranslationId(translationId, pageable)

    fun findById(id: Long): BibleBook =
        bookRepository.findByIdOrNull(id) ?: throwError(ErrorType.BOOK_NOT_FOUND, "id=$id")

    @Transactional
    fun create(translationId: Long, bookKey: BibleBookKey, bookOrder: Int, name: String, abbreviation: String, testamentType: BibleTestamentType): BibleBook =
        bookRepository.save(
            BibleBook(
                translationId = translationId,
                bookKey = bookKey,
                bookOrder = bookOrder,
                name = name,
                abbreviation = abbreviation,
                testamentType = testamentType,
            )
        )

    @Transactional
    fun update(id: Long, bookKey: BibleBookKey, bookOrder: Int, name: String, abbreviation: String, testamentType: BibleTestamentType): BibleBook {
        val existing = findById(id)
        val updated = BibleBook(
            id = existing.id,
            translationId = existing.translationId,
            bookKey = bookKey,
            bookOrder = bookOrder,
            name = name,
            abbreviation = abbreviation,
            testamentType = testamentType,
        )
        return bookRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        bookRepository.delete(entity)
    }
}
