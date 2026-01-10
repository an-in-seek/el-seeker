package com.elseeker.game.adapter.`in`.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/game")
class GameWebController {

    @GetMapping
    fun showGameList(): String {
        return "game/game"
    }

    @GetMapping("/bible-quiz")
    fun showBibleQuiz(): String {
        return "game/bible-quiz"
    }

    @GetMapping("/bible-quiz/map")
    fun showBibleQuizMap(): String {
        return "game/bible-quiz-map"
    }
}
