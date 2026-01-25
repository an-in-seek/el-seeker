package com.elseeker.game.application.component

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.stereotype.Component

@Component
class QuizStageValidator {

    fun requireStageNumberInRange(stageNumber: Int, stageCount: Int) {
        if (stageNumber < 1 || stageCount < stageNumber) {
            throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        }
    }

    fun ensureQuestionStageMatch(stageNumber: Int, requestedStage: Int, questionId: Long) {
        if (stageNumber != requestedStage) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$requestedStage", "questionId=$questionId")
        }
    }

    fun requireQuestionIndexNotAhead(requestIndex: Int, currentIndex: Int) {
        if (requestIndex > currentIndex) {
            throwError(
                ErrorType.INVALID_PARAMETER,
                "questionIndex=$requestIndex",
                "currentQuestionIndex=$currentIndex"
            )
        }
    }
}
