package com.elseeker.bible.adapter.input.web

import com.elseeker.bible.adapter.input.web.response.BibleViewResponse
import com.elseeker.bible.application.service.BibleService
import com.elseeker.bible.domain.vo.BibleTranslationType
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
    fun showTranslations(
        model: Model,
        @RequestParam(required = false) dev: String?
    ): String {
        val devEnabled = dev == "1" || dev.equals("true", ignoreCase = true)
        val translations = bibleService.getTranslations()
            .let { items ->
                if (devEnabled) {
                    items
                } else {
                    items.filter { it.translationType != BibleTranslationType.NKRV }
                }
            } // TODO: 추후 개역개정 저작권 무료화되면 제거 예정
            .map(BibleViewResponse.Translation::from)
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
