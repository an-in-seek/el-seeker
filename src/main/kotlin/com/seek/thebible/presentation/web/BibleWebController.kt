package com.seek.thebible.presentation.web

import com.seek.thebible.application.bible.BibleFacade
import com.seek.thebible.presentation.web.response.BibleViewResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/bible")
class BibleWebController(
    private val bibleFacade: BibleFacade
) {

    @GetMapping("/translation")
    fun showTranslations(model: Model): String {
        val translations = bibleFacade.getTranslations().map(BibleViewResponse.Translation::from)
        model.addAttribute("translations", translations)
        return "translations"
    }

    @GetMapping("/book")
    fun showBooks(): String {
        return "books"
    }

    @GetMapping("/chapter")
    fun showChapters(): String {
        return "chapters"
    }

    @GetMapping("/verse")
    fun showVerses(): String {
        return "verses"
    }

    @GetMapping("/search")
    fun showSearch(): String {
        return "search"
    }
}
