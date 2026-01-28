package com.elseeker.game.adapter.input.web

import org.springframework.security.core.Authentication
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
    fun showBibleQuiz(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/game/bible-quiz")?.let { return it }
        return "game/bible-quiz"
    }

    @GetMapping("/bible-quiz/map")
    fun showBibleQuizMap(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/game/bible-quiz/map")?.let { return it }
        return "game/bible-quiz-map"
    }

    @GetMapping("/bible-typing")
    fun showBibleTyping(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/game/bible-typing")?.let { return it }
        return "game/bible-typing"
    }

    @GetMapping("/bible-ox-quiz")
    fun showBibleOxQuiz(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/game/bible-ox-quiz")?.let { return it }
        return "game/bible-ox-quiz"
    }

    private fun redirectIfUnauthenticated(authentication: Authentication?, returnUrl: String): String? {
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return "redirect:/web/auth/login?returnUrl=$returnUrl"
        }
        return null
    }
}
