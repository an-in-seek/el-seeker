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

    @GetMapping
    fun showDictionaryList(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        model: Model
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "term"))
        val dictionaryPage = bibleDictionaryService.getDictionaries(keyword, pageable)
            .map(BibleDictionaryViewResponse.ListItem::from)
        val pageNumbers = if (dictionaryPage.totalPages == 0) {
            emptyList()
        } else {
            (1..dictionaryPage.totalPages).toList()
        }

        model.addAttribute("dictionaryPage", dictionaryPage)
        model.addAttribute("keyword", keyword?.trim().orEmpty())
        model.addAttribute("pageNumbers", pageNumbers)
        return "dictionary-list"
    }

    @GetMapping("/{id}")
    fun showDictionaryDetail(
        @PathVariable id: Long,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        model: Model
    ): String {
        val dictionary = BibleDictionaryViewResponse.Detail.from(
            bibleDictionaryService.getDictionary(id)
        )
        val trimmedKeyword = keyword?.trim().orEmpty()
        val backLink = if (trimmedKeyword.isBlank()) {
            "/web/study/dictionary?page=$page&size=$size"
        } else {
            val encodedKeyword = URLEncoder.encode(trimmedKeyword, StandardCharsets.UTF_8)
            "/web/study/dictionary?keyword=$encodedKeyword&page=$page&size=$size"
        }

        model.addAttribute("dictionary", dictionary)
        model.addAttribute("backLink", backLink)
        return "dictionary-detail"
    }
}
