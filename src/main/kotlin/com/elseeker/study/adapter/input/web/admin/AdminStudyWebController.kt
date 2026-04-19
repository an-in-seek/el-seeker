package com.elseeker.study.adapter.input.web.admin

import com.elseeker.study.application.service.AdminDictionaryService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/web/admin")
class AdminStudyWebController(
    private val adminDictionaryService: AdminDictionaryService,
) {

    @GetMapping("/dictionaries")
    fun dictionaryList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) keyword: String?,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("term"))
        val result = adminDictionaryService.findAll(keyword, pageable)
        model.addAttribute("page", result)
        model.addAttribute("keyword", keyword.orEmpty())
        return "admin/study/admin-dictionary-list"
    }

    @GetMapping("/dictionaries/new")
    fun dictionaryNewForm(): String = "admin/study/admin-dictionary-form"

    @GetMapping("/dictionaries/{id}/edit")
    fun dictionaryEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("dictionary", adminDictionaryService.findById(id))
        return "admin/study/admin-dictionary-form"
    }
}
