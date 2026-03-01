package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.output.jpa.WordPuzzleEntryRepository
import com.elseeker.game.adapter.output.jpa.WordPuzzleRepository
import com.elseeker.game.domain.model.WordPuzzle
import com.elseeker.game.domain.model.WordPuzzleEntry
import com.elseeker.game.domain.vo.ClueType
import com.elseeker.game.domain.vo.PuzzleDirection
import com.elseeker.game.domain.vo.PuzzleStatus
import com.elseeker.game.domain.vo.QuizDifficulty
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional(readOnly = true)
class AdminWordPuzzleService(
    private val wordPuzzleRepository: WordPuzzleRepository,
    private val wordPuzzleEntryRepository: WordPuzzleEntryRepository,
    private val dictionaryRepository: DictionaryRepository,
) {

    // ── Puzzle CRUD ──

    fun findAllPuzzles(pageable: Pageable): Page<WordPuzzle> =
        wordPuzzleRepository.findAll(pageable)

    fun findPuzzleById(id: Long): WordPuzzle =
        wordPuzzleRepository.findByIdOrNull(id) ?: throwError(ErrorType.WORD_PUZZLE_NOT_FOUND, "id=$id")

    @Transactional
    fun createPuzzle(
        title: String,
        themeCode: String,
        difficultyCode: QuizDifficulty,
        boardWidth: Int,
        boardHeight: Int,
    ): WordPuzzle = wordPuzzleRepository.save(
        WordPuzzle(
            title = title,
            themeCode = themeCode,
            difficultyCode = difficultyCode,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
        )
    )

    @Transactional
    fun updatePuzzle(
        id: Long,
        title: String,
        themeCode: String,
        difficultyCode: QuizDifficulty,
        boardWidth: Int,
        boardHeight: Int,
    ): WordPuzzle {
        val existing = findPuzzleById(id)
        val updated = WordPuzzle(
            id = existing.id,
            title = title,
            themeCode = themeCode,
            difficultyCode = difficultyCode,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            puzzleStatusCode = existing.puzzleStatusCode,
            publishedAt = existing.publishedAt,
        )
        updated.createdAt = existing.createdAt
        return wordPuzzleRepository.save(updated)
    }

    @Transactional
    fun changeStatus(id: Long, newStatus: PuzzleStatus): WordPuzzle {
        val puzzle = findPuzzleById(id)
        validateStatusTransition(puzzle.puzzleStatusCode, newStatus)

        puzzle.puzzleStatusCode = newStatus
        if (newStatus == PuzzleStatus.PUBLISHED && puzzle.publishedAt == null) {
            puzzle.publishedAt = Instant.now()
        }
        return wordPuzzleRepository.save(puzzle)
    }

    @Transactional
    fun deletePuzzle(id: Long) {
        val puzzle = findPuzzleById(id)
        if (puzzle.puzzleStatusCode != PuzzleStatus.DRAFT) {
            throwError(ErrorType.CANNOT_DELETE_PUBLISHED_PUZZLE, "id=$id, status=${puzzle.puzzleStatusCode}")
        }
        wordPuzzleRepository.delete(puzzle)
    }

    // ── Entry CRUD ──

    fun findAllEntries(puzzleId: Long, pageable: Pageable): Page<WordPuzzleEntry> =
        wordPuzzleEntryRepository.findAllByPuzzleIdWithDictionary(puzzleId, pageable)

    fun findEntryById(puzzleId: Long, entryId: Long): WordPuzzleEntry {
        val entry = wordPuzzleEntryRepository.findByIdWithDictionary(entryId)
            ?: throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "id=$entryId")
        if (entry.wordPuzzle.id != puzzleId) {
            throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "id=$entryId, puzzleId=$puzzleId")
        }
        return entry
    }

    @Transactional
    fun createEntry(
        puzzleId: Long,
        dictionaryId: Long,
        answerText: String,
        directionCode: PuzzleDirection,
        startRow: Int,
        startCol: Int,
        clueNumber: Int,
        clueTypeCode: ClueType,
        clueText: String,
    ): WordPuzzleEntry {
        val puzzle = findPuzzleById(puzzleId)
        val dictionary = findDictionaryById(dictionaryId)

        if (wordPuzzleEntryRepository.existsByWordPuzzleIdAndClueNumberAndDirectionCode(puzzleId, clueNumber, directionCode)) {
            throwError(ErrorType.DUPLICATE_CLUE_ENTRY, "puzzleId=$puzzleId, clueNumber=$clueNumber, direction=$directionCode")
        }

        return wordPuzzleEntryRepository.save(
            WordPuzzleEntry(
                wordPuzzle = puzzle,
                dictionary = dictionary,
                answerText = answerText,
                directionCode = directionCode,
                startRow = startRow,
                startCol = startCol,
                clueNumber = clueNumber,
                clueTypeCode = clueTypeCode,
                clueText = clueText,
            )
        )
    }

    @Transactional
    fun updateEntry(
        puzzleId: Long,
        entryId: Long,
        dictionaryId: Long,
        answerText: String,
        directionCode: PuzzleDirection,
        startRow: Int,
        startCol: Int,
        clueNumber: Int,
        clueTypeCode: ClueType,
        clueText: String,
    ): WordPuzzleEntry {
        val existing = findEntryById(puzzleId, entryId)
        val dictionary = findDictionaryById(dictionaryId)

        val clueChanged = existing.clueNumber != clueNumber || existing.directionCode != directionCode
        if (clueChanged && wordPuzzleEntryRepository.existsByWordPuzzleIdAndClueNumberAndDirectionCode(
                existing.wordPuzzle.id!!, clueNumber, directionCode
            )
        ) {
            throwError(ErrorType.DUPLICATE_CLUE_ENTRY, "clueNumber=$clueNumber, direction=$directionCode")
        }

        val updated = WordPuzzleEntry(
            id = existing.id,
            wordPuzzle = existing.wordPuzzle,
            dictionary = dictionary,
            answerText = answerText,
            directionCode = directionCode,
            startRow = startRow,
            startCol = startCol,
            clueNumber = clueNumber,
            clueTypeCode = clueTypeCode,
            clueText = clueText,
            createdAt = existing.createdAt,
        )
        return wordPuzzleEntryRepository.save(updated)
    }

    @Transactional
    fun deleteEntry(puzzleId: Long, entryId: Long) {
        val entry = findEntryById(puzzleId, entryId)
        wordPuzzleEntryRepository.delete(entry)
    }

    // ── Dictionary Search ──

    fun searchDictionaries(term: String, pageable: Pageable): Page<Dictionary> =
        dictionaryRepository.findByTermContainingKo(term.trim(), pageable)

    // ── Private ──

    private fun findDictionaryById(id: Long): Dictionary =
        dictionaryRepository.findByIdOrNull(id) ?: throwError(ErrorType.DICTIONARY_NOT_FOUND, "id=$id")

    private fun validateStatusTransition(current: PuzzleStatus, target: PuzzleStatus) {
        val allowed = when (current) {
            PuzzleStatus.DRAFT -> setOf(PuzzleStatus.PUBLISHED)
            PuzzleStatus.PUBLISHED -> setOf(PuzzleStatus.ARCHIVED)
            PuzzleStatus.ARCHIVED -> setOf(PuzzleStatus.PUBLISHED)
        }
        if (target !in allowed) {
            throwError(ErrorType.INVALID_STATUS_TRANSITION, "${current}에서 ${target}(으)로 전환할 수 없습니다.")
        }
    }
}
