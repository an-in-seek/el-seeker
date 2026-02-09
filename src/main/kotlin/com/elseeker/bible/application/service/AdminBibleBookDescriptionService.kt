package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookDescriptionRepository
import com.elseeker.bible.domain.model.BibleBookDescription
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.neovisionaries.i18n.LanguageCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminBibleBookDescriptionService(
    private val bibleBookDescriptionRepository: BibleBookDescriptionRepository,
) {
    fun findAll(bookKey: BibleBookKey?, languageCode: LanguageCode?, pageable: Pageable): Page<BibleBookDescription> =
        when {
            bookKey != null && languageCode != null -> bibleBookDescriptionRepository.findByBookKeyAndLanguageCode(bookKey, languageCode, pageable)
            bookKey != null -> bibleBookDescriptionRepository.findByBookKey(bookKey, pageable)
            languageCode != null -> bibleBookDescriptionRepository.findByLanguageCode(languageCode, pageable)
            else -> bibleBookDescriptionRepository.findAll(pageable)
        }

    fun findById(id: Long): BibleBookDescription =
        bibleBookDescriptionRepository.findByIdOrNull(id) ?: throwError(ErrorType.BOOK_DESCRIPTION_NOT_FOUND, "id=$id")

    @Transactional
    fun create(
        bookKey: BibleBookKey,
        languageCode: LanguageCode,
        summary: String,
        author: String,
        writtenYear: String,
        historicalPeriod: String,
        background: String,
        content: String,
    ): BibleBookDescription =
        bibleBookDescriptionRepository.save(
            BibleBookDescription(
                bookKey = bookKey,
                languageCode = languageCode,
                summary = summary,
                author = author,
                writtenYear = writtenYear,
                historicalPeriod = historicalPeriod,
                background = background,
                content = content,
            )
        )

    @Transactional
    fun update(
        id: Long,
        bookKey: BibleBookKey,
        languageCode: LanguageCode,
        summary: String,
        author: String,
        writtenYear: String,
        historicalPeriod: String,
        background: String,
        content: String,
    ): BibleBookDescription {
        val existing = findById(id)
        val updated = BibleBookDescription(
            id = existing.id,
            bookKey = bookKey,
            languageCode = languageCode,
            summary = summary,
            author = author,
            writtenYear = writtenYear,
            historicalPeriod = historicalPeriod,
            background = background,
            content = content,
        )
        return bibleBookDescriptionRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        bibleBookDescriptionRepository.delete(entity)
    }
}
