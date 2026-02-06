package com.elseeker.game.adapter.input.api.admin

import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.game.adapter.input.api.admin.request.AdminQuizQuestionRequest
import com.elseeker.game.adapter.input.api.admin.request.AdminQuizStageRequest
import com.elseeker.game.application.service.AdminQuizService
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizStage
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/quiz")
class AdminQuizApi(
    private val adminQuizService: AdminQuizService,
) {
    @GetMapping("/stages")
    fun listStages(pageable: Pageable): ResponseEntity<AdminPageResponse<QuizStageItem>> {
        val effective = if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by("stageNumber"))
        }
        val summaries = adminQuizService.getStageSummaries()
        val result = adminQuizService.findStages(effective)
        return ResponseEntity.ok(
            AdminPageResponse.from(result) { stage ->
                QuizStageItem.from(stage, summaries[stage.stageNumber] ?: 0)
            }
        )
    }

    @GetMapping("/stages/{id}")
    fun getStage(@PathVariable id: Long): ResponseEntity<QuizStageItem> =
        ResponseEntity.ok(QuizStageItem.from(adminQuizService.findStageById(id)))

    @PostMapping("/stages")
    fun createStage(@RequestBody request: AdminQuizStageRequest): ResponseEntity<QuizStageItem> {
        val created = adminQuizService.createStage(request.stageNumber, request.title)
        return ResponseEntity.ok(QuizStageItem.from(created))
    }

    @PutMapping("/stages/{id}")
    fun updateStage(
        @PathVariable id: Long,
        @RequestBody request: AdminQuizStageRequest
    ): ResponseEntity<QuizStageItem> {
        val updated = adminQuizService.updateStage(id, request.stageNumber, request.title)
        return ResponseEntity.ok(QuizStageItem.from(updated))
    }

    @DeleteMapping("/stages/{id}")
    fun deleteStage(@PathVariable id: Long): ResponseEntity<Void> {
        adminQuizService.deleteStage(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/stages/{stageId}/questions")
    fun listQuestions(
        @PathVariable stageId: Long,
        pageable: Pageable
    ): ResponseEntity<AdminPageResponse<QuizQuestionItem>> {
        val effective = if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by("id"))
        }
        val result = adminQuizService.findQuestionsByStage(stageId, effective)
        return ResponseEntity.ok(AdminPageResponse.from(result) { QuizQuestionItem.from(it) })
    }

    @GetMapping("/questions/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<QuizQuestionDetail> =
        ResponseEntity.ok(QuizQuestionDetail.from(adminQuizService.findQuestionById(id)))

    @PostMapping("/stages/{stageId}/questions")
    fun createQuestion(
        @PathVariable stageId: Long,
        @RequestBody request: AdminQuizQuestionRequest
    ): ResponseEntity<QuizQuestionDetail> {
        val created = adminQuizService.createQuestion(
            stageId = stageId,
            questionText = request.questionText,
            answerIndex = request.answerIndex,
            difficulty = request.difficulty,
            options = request.options.map { AdminQuizService.OptionPayload(it.optionIndex, it.optionText) }
        )
        return ResponseEntity.ok(QuizQuestionDetail.from(created))
    }

    @PutMapping("/questions/{id}")
    fun updateQuestion(
        @PathVariable id: Long,
        @RequestBody request: AdminQuizQuestionRequest
    ): ResponseEntity<QuizQuestionDetail> {
        val updated = adminQuizService.updateQuestion(
            id = id,
            questionText = request.questionText,
            answerIndex = request.answerIndex,
            difficulty = request.difficulty,
            options = request.options.map { AdminQuizService.OptionPayload(it.optionIndex, it.optionText) }
        )
        return ResponseEntity.ok(QuizQuestionDetail.from(updated))
    }

    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        adminQuizService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    data class QuizStageItem(
        val id: Long,
        val stageNumber: Int,
        val title: String?,
        val questionCount: Long,
    ) {
        companion object {
            fun from(stage: QuizStage, questionCount: Long = 0) = QuizStageItem(
                id = stage.id ?: 0L,
                stageNumber = stage.stageNumber,
                title = stage.title,
                questionCount = questionCount,
            )
        }
    }

    data class QuizQuestionItem(
        val id: Long,
        val stageId: Long,
        val stageNumber: Int,
        val questionText: String,
        val answerIndex: Int,
        val difficulty: String?
    ) {
        companion object {
            fun from(question: QuizQuestion) = QuizQuestionItem(
                id = question.id ?: 0L,
                stageId = question.stage.id ?: 0L,
                stageNumber = question.stage.stageNumber,
                questionText = question.questionText,
                answerIndex = question.answerIndex,
                difficulty = question.difficulty?.name
            )
        }
    }

    data class QuizQuestionDetail(
        val id: Long,
        val stageId: Long,
        val stageNumber: Int,
        val questionText: String,
        val answerIndex: Int,
        val difficulty: String?,
        val options: List<OptionItem>,
    ) {
        companion object {
            fun from(question: QuizQuestion) = QuizQuestionDetail(
                id = question.id ?: 0L,
                stageId = question.stage.id ?: 0L,
                stageNumber = question.stage.stageNumber,
                questionText = question.questionText,
                answerIndex = question.answerIndex,
                difficulty = question.difficulty?.name,
                options = question.options.sortedBy { it.optionIndex }.map { OptionItem.from(it.optionIndex, it.optionText) }
            )
        }
    }

    data class OptionItem(
        val optionIndex: Int,
        val optionText: String
    ) {
        companion object {
            fun from(optionIndex: Int, optionText: String) = OptionItem(optionIndex, optionText)
        }
    }
}
