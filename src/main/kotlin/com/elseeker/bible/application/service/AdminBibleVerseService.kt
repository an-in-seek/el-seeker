package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleVerseRepository
import com.elseeker.bible.domain.model.BibleVerse
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminBibleVerseService(
    private val verseRepository: BibleVerseRepository,
) {
    fun findByChapterId(chapterId: Long, pageable: Pageable): Page<BibleVerse> =
        verseRepository.findByChapterId(chapterId, pageable)

    fun findById(id: Long): BibleVerse =
        verseRepository.findByIdOrNull(id) ?: throwError(ErrorType.VERSE_NOT_FOUND, "id=$id")

    @Transactional
    fun create(chapterId: Long, verseNumber: Int, text: String): BibleVerse =
        verseRepository.save(BibleVerse(chapterId = chapterId, verseNumber = verseNumber, text = text))

    @Transactional
    fun update(id: Long, verseNumber: Int, text: String): BibleVerse {
        val existing = findById(id)
        val updated = BibleVerse(id = existing.id, chapterId = existing.chapterId, verseNumber = verseNumber, text = text)
        return verseRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        verseRepository.delete(entity)
    }
}
