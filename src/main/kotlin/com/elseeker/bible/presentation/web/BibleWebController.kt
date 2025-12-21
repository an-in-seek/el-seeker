package com.elseeker.bible.presentation.web

import com.elseeker.bible.application.bible.service.BibleService
import com.elseeker.bible.presentation.web.response.BibleViewResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/bible")
class BibleWebController(
    private val bibleService: BibleService
) {

    @GetMapping("/translation")
    fun showTranslations(model: Model): String {
        val translations = bibleService.getTranslations().map(BibleViewResponse.Translation::from)
        model.addAttribute("translations", translations)
        return "translation"
    }

    @GetMapping("/book")
    fun showBooks(): String {
        return "book"
    }

    @GetMapping("/book/description")
    fun showBookDescription(): String {
        return "book-description"
    }

    @GetMapping("/chapter")
    fun showChapters(): String {
        return "chapter"
    }

    @GetMapping("/verse")
    fun showVerses(): String {
        return "verse"
    }

    @GetMapping("/search")
    fun showSearch(): String {
        return "search"
    }
}
