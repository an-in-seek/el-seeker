package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.client.request.*
import com.elseeker.game.adapter.input.api.client.response.*
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.domain.model.*
import com.elseeker.game.domain.vo.AttemptStatus
import com.elseeker.game.domain.event.GameCompletedEvent
import com.elseeker.game.domain.vo.GameType
import com.elseeker.game.domain.vo.HintType
import com.elseeker.game.domain.vo.PuzzleDirection
import com.elseeker.game.domain.vo.PuzzleStatus
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class WordPuzzleService(
    private val puzzleRepository: WordPuzzleRepository,
    private val entryRepository: WordPuzzleEntryRepository,
    private val attemptRepository: WordPuzzleAttemptRepository,
    private val cellRepository: WordPuzzleAttemptCellRepository,
    private val hintUsageRepository: WordPuzzleHintUsageRepository,
    private val progressRepository: MemberDictionaryProgressRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val memberRepository: MemberRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    // ── 1. 퍼즐 목록 조회 ──

    @Transactional(readOnly = true)
    fun getPuzzles(
        themeCode: String?,
        difficultyCode: String?,
        pageable: Pageable,
        memberUid: UUID
    ): Page<PuzzleSummaryResponse> {
        val member = getMember(memberUid)
        val difficulty = difficultyCode?.let {
            try {
                com.elseeker.game.domain.vo.QuizDifficulty.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        val puzzlePage = puzzleRepository.findPublishedPuzzles(
            themeCode = themeCode,
            difficultyCode = difficulty,
            pageable = pageable
        )

        val inProgressAttempts = attemptRepository.findAllByMemberAndStatus(member)
        val attemptByPuzzleId = inProgressAttempts.associateBy { it.wordPuzzle.id }

        return puzzlePage.map { puzzle ->
            PuzzleSummaryResponse(
                puzzleId = requireNotNull(puzzle.id),
                title = puzzle.title,
                themeCode = puzzle.themeCode,
                difficultyCode = puzzle.difficultyCode.name,
                boardWidth = puzzle.boardWidth,
                boardHeight = puzzle.boardHeight,
                publishedAt = puzzle.publishedAt,
                inProgressAttemptId = attemptByPuzzleId[puzzle.id]?.id
            )
        }
    }

    // ── 2. 퍼즐 시작 (신규) ──

    @Transactional
    fun startPuzzle(puzzleId: Long, memberUid: UUID): PuzzleAttemptResponse {
        val member = getMember(memberUid)
        val puzzle = puzzleRepository.findByIdWithEntries(puzzleId)
            ?: throwError(ErrorType.WORD_PUZZLE_NOT_FOUND, "puzzleId=$puzzleId")

        // PUBLISHED 상태가 아니면 신규 시작 불가
        if (puzzle.puzzleStatusCode != PuzzleStatus.PUBLISHED) {
            throwError(ErrorType.PUZZLE_NOT_AVAILABLE, "puzzleId=$puzzleId, status=${puzzle.puzzleStatusCode}")
        }

        // 이미 진행 중인 attempt가 있으면 자동 이어하기
        val existing = attemptRepository.findByMemberAndPuzzleIdAndStatus(member, puzzleId)
        if (existing != null) {
            val existingEntries = entryRepository.findAllByPuzzleIdWithDictionary(puzzleId)
            val existingCells = ensureCellsExist(existing, existingEntries)
            return buildAttemptResponse(existing, puzzle, existingEntries, existingCells)
        }

        // 새 attempt 생성
        val attempt = WordPuzzleAttempt(
            member = member,
            wordPuzzle = puzzle
        )
        val savedAttempt = attemptRepository.save(attempt)

        val entries = entryRepository.findAllByPuzzleIdWithDictionary(puzzleId)
        val cells = createCellsForAttempt(savedAttempt, entries)

        return buildAttemptResponse(savedAttempt, puzzle, entries, cells)
    }

    // ── 3. 퍼즐 이어하기 ──

    @Transactional
    fun resumeAttempt(puzzleId: Long, attemptId: Long, memberUid: UUID): PuzzleAttemptResponse {
        val member = getMember(memberUid)
        val attempt = attemptRepository.findByIdAndMemberWithPuzzle(attemptId, member)
            ?: throwError(ErrorType.WORD_PUZZLE_ATTEMPT_NOT_FOUND, "attemptId=$attemptId")

        if (attempt.wordPuzzle.id != puzzleId) {
            throwError(ErrorType.WORD_PUZZLE_ATTEMPT_NOT_FOUND, "puzzleId=$puzzleId, attemptId=$attemptId")
        }

        val entries = entryRepository.findAllByPuzzleIdWithDictionary(puzzleId)
        val cells = ensureCellsExist(attempt, entries)

        return buildAttemptResponse(attempt, attempt.wordPuzzle, entries, cells)
    }

    // ── 4. 셀 저장 (자동 저장) ──

    @Transactional
    fun saveCells(
        puzzleId: Long,
        attemptId: Long,
        request: CellSaveRequest,
        memberUid: UUID
    ) {
        val member = getMember(memberUid)
        val attempt = getAttemptOrThrow(attemptId, member)
        validateNotCompleted(attempt)

        if (attempt.wordPuzzle.id != puzzleId) {
            throwError(ErrorType.WORD_PUZZLE_ATTEMPT_NOT_FOUND, "puzzleId=$puzzleId, attemptId=$attemptId")
        }

        attempt.updateElapsedSeconds(request.elapsedSeconds)

        request.cells.forEach { cellInput ->
            val cell = cellRepository.findByAttemptIdAndPosition(attemptId, cellInput.row, cellInput.col)
            cell?.updateLetter(cellInput.letter)
        }
    }

    // ── 5. 힌트 — 글자 공개 ──

    @Transactional
    fun revealLetter(
        puzzleId: Long,
        attemptId: Long,
        request: RevealLetterRequest,
        memberUid: UUID
    ): RevealLetterResponse {
        val member = getMember(memberUid)
        val attempt = getAttemptOrThrow(attemptId, member)
        validateNotCompleted(attempt)

        val entry = entryRepository.findByIdWithDictionary(request.entryId)
            ?: throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "entryId=${request.entryId}")

        if (entry.wordPuzzle.id != attempt.wordPuzzle.id) {
            throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "entryId=${request.entryId}")
        }

        val cell = cellRepository.findByAttemptIdAndPosition(attemptId, request.row, request.col)
            ?: throwError(ErrorType.INVALID_PARAMETER, "row=${request.row}, col=${request.col}")

        if (cell.isRevealed) {
            throwError(ErrorType.CELL_ALREADY_REVEALED, "row=${request.row}, col=${request.col}")
        }

        // 정답 글자 계산
        val letterIndex = getLetterIndex(entry, request.row, request.col)
        val correctLetter = entry.answerText[letterIndex].toString()

        cell.reveal(correctLetter)
        attempt.updateElapsedSeconds(request.elapsedSeconds)
        attempt.incrementHintUsage()

        // 힌트 사용 기록
        val hintUsage = WordPuzzleHintUsage(
            attempt = attempt,
            entry = entry,
            rowIndex = request.row,
            colIndex = request.col,
            hintTypeCode = HintType.REVEAL_LETTER
        )
        hintUsageRepository.save(hintUsage)

        return RevealLetterResponse(
            row = request.row,
            col = request.col,
            letter = correctLetter,
            hintUsageCount = attempt.hintUsageCount
        )
    }

    // ── 6. 힌트 — 단어 확인 ──

    @Transactional
    fun checkWord(
        puzzleId: Long,
        attemptId: Long,
        request: CheckWordRequest,
        memberUid: UUID
    ): CheckWordResponse {
        val member = getMember(memberUid)
        val attempt = getAttemptOrThrow(attemptId, member)
        validateNotCompleted(attempt)

        val entry = entryRepository.findByIdWithDictionary(request.entryId)
            ?: throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "entryId=${request.entryId}")

        if (entry.wordPuzzle.id != attempt.wordPuzzle.id) {
            throwError(ErrorType.WORD_PUZZLE_ENTRY_NOT_FOUND, "entryId=${request.entryId}")
        }

        attempt.updateElapsedSeconds(request.elapsedSeconds)
        attempt.incrementHintUsage()

        // 힌트 사용 기록 (CHECK_WORD는 row/col이 null)
        val hintUsage = WordPuzzleHintUsage(
            attempt = attempt,
            entry = entry,
            hintTypeCode = HintType.CHECK_WORD
        )
        hintUsageRepository.save(hintUsage)

        // 셀별 정답 확인
        val results = mutableListOf<CellCheckResult>()
        for (i in 0 until entry.length) {
            val row = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startRow else entry.startRow + i
            val col = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startCol + i else entry.startCol
            val correctLetter = entry.answerText[i].toString()

            val cell = cellRepository.findByAttemptIdAndPosition(attemptId, row, col)
            val isCorrect = cell?.inputLetter != null && cell.inputLetter == correctLetter

            results.add(CellCheckResult(row = row, col = col, correct = isCorrect))
        }

        return CheckWordResponse(
            results = results,
            hintUsageCount = attempt.hintUsageCount
        )
    }

    // ── 7. 전체 제출 ──

    @Transactional
    fun submit(
        puzzleId: Long,
        attemptId: Long,
        request: SubmitRequest,
        memberUid: UUID
    ): Any {
        val member = getMember(memberUid)
        val attempt = getAttemptOrThrow(attemptId, member)
        validateNotCompleted(attempt)

        if (attempt.wordPuzzle.id != puzzleId) {
            throwError(ErrorType.WORD_PUZZLE_ATTEMPT_NOT_FOUND, "puzzleId=$puzzleId, attemptId=$attemptId")
        }

        attempt.updateElapsedSeconds(request.elapsedSeconds)

        val entries = entryRepository.findAllByPuzzleIdWithDictionary(puzzleId)
        val cells = cellRepository.findAllByAttemptId(attemptId)
        val cellMap = cells.associateBy { Pair(it.rowIndex, it.colIndex) }

        // 빈 셀 확인
        val emptyCells = cells.filter { it.inputLetter == null && !it.isRevealed }
        if (emptyCells.isNotEmpty()) {
            throwError(ErrorType.EMPTY_CELLS_EXIST)
        }

        // 정답 검증
        val wrongCells = mutableListOf<WrongCellResponse>()

        entries.forEach { entry ->
            for (i in 0 until entry.length) {
                val row = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startRow else entry.startRow + i
                val col = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startCol + i else entry.startCol
                val correctLetter = entry.answerText[i].toString()

                val cell = cellMap[Pair(row, col)]
                if (cell?.inputLetter != correctLetter) {
                    wrongCells.add(WrongCellResponse(row = row, col = col))
                }
            }
        }

        if (wrongCells.isNotEmpty()) {
            // 오답
            attempt.incrementWrongSubmission()
            return SubmitWrongResponse(
                wrongCells = wrongCells.distinct(),
                wrongSubmissionCount = attempt.wrongSubmissionCount,
                elapsedSeconds = attempt.elapsedSeconds
            )
        }

        // 정답 - 점수 산정 및 완료 처리
        val score = attempt.calculateScore(attempt.wordPuzzle.difficultyCode)
        attempt.complete(score)

        eventPublisher.publishEvent(GameCompletedEvent(member.id!!, GameType.WORD_PUZZLE))

        // 단어별 진행도 업데이트
        entries.forEach { entry ->
            val progress = progressRepository.findByMemberAndDictionaryId(member, requireNotNull(entry.dictionary.id))
            if (progress != null) {
                progress.incrementSolved()
            } else {
                val newProgress = MemberDictionaryProgress(
                    member = member,
                    dictionary = entry.dictionary
                )
                newProgress.incrementSolved()
                progressRepository.save(newProgress)
            }
        }

        // 학습 정보 조회 (references를 JOIN FETCH로 일괄 로딩하여 N+1 방지)
        val dictionaryIds = entries.map { requireNotNull(it.dictionary.id) }.distinct()
        val dictionaries = dictionaryRepository.findAllByIdWithReferences(dictionaryIds)
        val words = dictionaries.map { word ->
            WordDetailResponse(
                surfaceForm = word.term,
                dictionaryDefinition = word.description ?: "",
                originalLanguageCode = word.originalLanguageCode?.name,
                originalLexeme = word.originalLexeme,
                references = word.references.map { ref ->
                    WordReferenceResponse(
                        verseReference = ref.verseLabel,
                        verseExcerpt = ""
                    )
                }
            )
        }

        return SubmitCorrectResponse(
            score = score,
            elapsedSeconds = attempt.elapsedSeconds,
            hintUsageCount = attempt.hintUsageCount,
            wrongSubmissionCount = attempt.wrongSubmissionCount,
            words = words
        )
    }

    // ── Private helpers ──

    private fun getMember(memberUid: UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }

    private fun getAttemptOrThrow(attemptId: Long, member: Member): WordPuzzleAttempt {
        return attemptRepository.findByIdAndMemberWithPuzzle(attemptId, member)
            ?: throwError(ErrorType.WORD_PUZZLE_ATTEMPT_NOT_FOUND, "attemptId=$attemptId")
    }

    private fun validateNotCompleted(attempt: WordPuzzleAttempt) {
        if (attempt.isCompleted()) {
            throwError(ErrorType.ATTEMPT_ALREADY_COMPLETED, "attemptId=${attempt.id}")
        }
    }

    private fun createCellsForAttempt(
        attempt: WordPuzzleAttempt,
        entries: List<WordPuzzleEntry>
    ): List<WordPuzzleAttemptCell> {
        val cellPositions = mutableSetOf<Pair<Int, Int>>()
        entries.forEach { entry ->
            for (i in 0 until entry.length) {
                val row = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startRow else entry.startRow + i
                val col = if (entry.directionCode == PuzzleDirection.ACROSS) entry.startCol + i else entry.startCol
                cellPositions.add(Pair(row, col))
            }
        }
        val cells = cellPositions.map { (row, col) ->
            WordPuzzleAttemptCell(
                attempt = attempt,
                rowIndex = row,
                colIndex = col
            )
        }
        return cellRepository.saveAll(cells)
    }

    private fun ensureCellsExist(
        attempt: WordPuzzleAttempt,
        entries: List<WordPuzzleEntry>
    ): List<WordPuzzleAttemptCell> {
        val cells = cellRepository.findAllByAttemptId(requireNotNull(attempt.id))
        if (cells.isNotEmpty()) return cells
        return createCellsForAttempt(attempt, entries)
    }

    private fun getLetterIndex(entry: WordPuzzleEntry, row: Int, col: Int): Int {
        return if (entry.directionCode == PuzzleDirection.ACROSS) {
            col - entry.startCol
        } else {
            row - entry.startRow
        }
    }

    private fun buildAttemptResponse(
        attempt: WordPuzzleAttempt,
        puzzle: WordPuzzle,
        entries: List<WordPuzzleEntry>,
        cells: List<WordPuzzleAttemptCell>
    ): PuzzleAttemptResponse {
        return PuzzleAttemptResponse(
            attemptId = requireNotNull(attempt.id),
            title = puzzle.title,
            elapsedSeconds = attempt.elapsedSeconds,
            board = BoardResponse(
                width = puzzle.boardWidth,
                height = puzzle.boardHeight
            ),
            entries = entries.map { entry ->
                EntryResponse(
                    entryId = requireNotNull(entry.id),
                    clueNumber = entry.clueNumber,
                    directionCode = entry.directionCode.name,
                    startRow = entry.startRow,
                    startCol = entry.startCol,
                    length = entry.length,
                    clueTypeCode = entry.clueTypeCode.name,
                    clueText = entry.clueText
                )
            },
            cells = cells.map { cell ->
                CellResponse(
                    row = cell.rowIndex,
                    col = cell.colIndex,
                    inputLetter = cell.inputLetter,
                    isRevealed = cell.isRevealed
                )
            }
        )
    }
}
