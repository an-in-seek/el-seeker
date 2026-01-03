package com.elseeker.bible.presentation.web.bible

import com.elseeker.bible.application.bible.service.BibleService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/web/bible")
class BibleWebController(
    private val bibleService: BibleService
) {

    @GetMapping("/translation")
    fun showTranslations(model: Model): String {
        val translations = bibleService.getTranslations().map(BibleViewResponse.Translation::from)
        model.addAttribute("translations", translations)
        return "bible/translation-list"
    }

    @GetMapping("/book")
    fun showBooks(): String {
        return "bible/book-list"
    }

    @GetMapping("/book/description")
    fun showBookDescription(): String {
        return "bible/book-description"
    }

    @GetMapping("/chapter")
    fun showChapters(): String {
        return "bible/chapter-list"
    }

    @GetMapping("/verse")
    fun showVerses(): String {
        return "bible/verse-list"
    }

    @GetMapping("/search")
    fun showSearch(
        @RequestParam(required = false) keyword: String?,
        model: Model
    ): String {
        model.addAttribute("keyword", keyword?.trim().orEmpty())
        return "bible/search"
    }
}