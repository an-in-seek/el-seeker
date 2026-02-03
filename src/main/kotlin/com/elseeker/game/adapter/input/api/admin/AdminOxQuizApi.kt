package com.elseeker.game.adapter.input.api.admin

import com.elseeker.common.adapter.input.api.response.AdminPageResponse
import com.elseeker.game.adapter.input.api.admin.request.AdminOxQuestionRequest
import com.elseeker.game.adapter.input.api.admin.request.AdminOxStageRequest
import com.elseeker.game.application.service.AdminOxQuizService
import com.elseeker.game.domain.model.OxQuestion
import com.elseeker.game.domain.model.OxStage
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/ox")
class AdminOxQuizApi(
    private val adminOxQuizService: AdminOxQuizService,
) {
    @GetMapping("/stages")
    fun listStages(pageable: Pageable): ResponseEntity<AdminPageResponse<OxStageItem>> {
        val effective = if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.ASC, "stageNumber"))
        }
        val counts = adminOxQuizService.getQuestionCountsByStage()
        val result = adminOxQuizService.findStages(effective)
        return ResponseEntity.ok(
            AdminPageResponse.from(result) { stage ->
                OxStageItem.from(stage, counts[stage.stageNumber] ?: 0)
            }
        )
    }

    @GetMapping("/stages/{id}")
    fun getStage(@PathVariable id: Long): ResponseEntity<OxStageItem> =
        ResponseEntity.ok(OxStageItem.from(adminOxQuizService.findStageById(id)))

    @PostMapping("/stages")
    fun createStage(@RequestBody request: AdminOxStageRequest): ResponseEntity<OxStageItem> {
        val created = adminOxQuizService.createStage(request.stageNumber, request.bookName)
        return ResponseEntity.ok(OxStageItem.from(created))
    }

    @PutMapping("/stages/{id}")
    fun updateStage(
        @PathVariable id: Long,
        @RequestBody request: AdminOxStageRequest
    ): ResponseEntity<OxStageItem> {
        val updated = adminOxQuizService.updateStage(id, request.stageNumber, request.bookName)
        return ResponseEntity.ok(OxStageItem.from(updated))
    }

    @DeleteMapping("/stages/{id}")
    fun deleteStage(@PathVariable id: Long): ResponseEntity<Void> {
        adminOxQuizService.deleteStage(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/stages/{stageId}/questions")
    fun listQuestions(
        @PathVariable stageId: Long,
        pageable: Pageable
    ): ResponseEntity<AdminPageResponse<OxQuestionItem>> {
        val effective = if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by("orderIndex"))
        }
        val result = adminOxQuizService.findQuestionsByStage(stageId, effective)
        return ResponseEntity.ok(AdminPageResponse.from(result) { OxQuestionItem.from(it) })
    }

    @GetMapping("/questions/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<OxQuestionItem> =
        ResponseEntity.ok(OxQuestionItem.from(adminOxQuizService.findQuestionById(id)))

    @PostMapping("/stages/{stageId}/questions")
    fun createQuestion(
        @PathVariable stageId: Long,
        @RequestBody request: AdminOxQuestionRequest
    ): ResponseEntity<OxQuestionItem> {
        val created = adminOxQuizService.createQuestion(
            stageId = stageId,
            questionText = request.questionText,
            correctAnswer = request.correctAnswer,
            difficulty = request.difficulty,
            orderIndex = request.orderIndex
        )
        return ResponseEntity.ok(OxQuestionItem.from(created))
    }

    @PutMapping("/questions/{id}")
    fun updateQuestion(
        @PathVariable id: Long,
        @RequestBody request: AdminOxQuestionRequest
    ): ResponseEntity<OxQuestionItem> {
        val updated = adminOxQuizService.updateQuestion(
            id = id,
            questionText = request.questionText,
            correctAnswer = request.correctAnswer,
            difficulty = request.difficulty,
            orderIndex = request.orderIndex
        )
        return ResponseEntity.ok(OxQuestionItem.from(updated))
    }

    @DeleteMapping("/questions/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        adminOxQuizService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    data class OxStageItem(
        val id: Long,
        val stageNumber: Int,
        val bookName: String,
        val questionCount: Int,
    ) {
        companion object {
            fun from(stage: OxStage, questionCount: Int = 0) = OxStageItem(
                id = stage.id ?: 0L,
                stageNumber = stage.stageNumber,
                bookName = stage.bookName,
                questionCount = questionCount,
            )
        }
    }

    data class OxQuestionItem(
        val id: Long,
        val stageId: Long,
        val stageNumber: Int,
        val questionText: String,
        val correctAnswer: Boolean,
        val difficulty: String,
        val orderIndex: Int
    ) {
        companion object {
            fun from(question: OxQuestion) = OxQuestionItem(
                id = question.id ?: 0L,
                stageId = question.stage.id ?: 0L,
                stageNumber = question.stage.stageNumber,
                questionText = question.questionText,
                correctAnswer = question.correctAnswer,
                difficulty = question.difficulty.name,
                orderIndex = question.orderIndex
            )
        }
    }
}
