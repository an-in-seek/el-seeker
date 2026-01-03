package com.elseeker.bible.presentation.web

import com.elseeker.bible.application.bible.service.BibleDictionaryService
import com.elseeker.bible.presentation.web.response.BibleDictionaryViewResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/web/study/dictionary")
class BibleDictionaryWebController(
    private val bibleDictionaryService: BibleDictionaryService
) {
    @GetMapping
    fun showDictionaryList(
        @RequestParam(required = false) keyword: String?,
        model: Model
    ): String {
        model.addAttribute("keyword", normalizeKeyword(keyword))
        return "/study/dictionary-list"
    }

    @GetMapping("/{id}")
    fun showDictionaryDetail(
        @PathVariable id: Long,
        @RequestParam(required = false) keyword: String?,
        model: Model
    ): String {
        val dictionary = BibleDictionaryViewResponse.Detail.from(bibleDictionaryService.getDictionary(id))
        val backLink = buildBackLink(keyword)
        model.addAttribute("dictionary", dictionary)
        model.addAttribute("backLink", backLink)
        return "/study/dictionary-detail"
    }

    // --------------------- Private Methods ---------------------
    private fun normalizeKeyword(keyword: String?): String {
        return keyword?.trim().orEmpty()
    }

    private fun buildBackLink(keyword: String?): String {
        val trimmedKeyword = normalizeKeyword(keyword)
        if (trimmedKeyword.isBlank()) {
            return "/web/study/dictionary"
        }
        val encodedKeyword = URLEncoder.encode(trimmedKeyword, StandardCharsets.UTF_8)
        return "/web/study/dictionary?keyword=$encodedKeyword"
    }
}
