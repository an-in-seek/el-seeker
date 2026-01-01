package com.elseeker.bible.presentation.web

import com.elseeker.bible.application.bible.service.BibleDictionaryService
import com.elseeker.bible.presentation.web.response.BibleDictionaryViewResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 10
    }

    @GetMapping
    fun showDictionaryList(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "$DEFAULT_PAGE") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_SIZE") size: Int,
        model: Model
    ): String {
        val pageable = createPageRequest(page, size)
        val dictionaryPage = bibleDictionaryService.getDictionaries(keyword, pageable).map(BibleDictionaryViewResponse.ListItem::from)
        val pageNumbers = buildPageNumbers(dictionaryPage.totalPages)
        model.addAttribute("dictionaryPage", dictionaryPage)
        model.addAttribute("keyword", normalizeKeyword(keyword))
        model.addAttribute("pageNumbers", pageNumbers)
        return "dictionary-list"
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
        return "dictionary-detail"
    }

    private fun createPageRequest(page: Int, size: Int): PageRequest {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "term"))
    }

    private fun buildPageNumbers(totalPages: Int): List<Int> {
        return if (totalPages == 0) emptyList() else (1..totalPages).toList()
    }

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
