package com.elseeker.member.adapter.input.web.admin

import com.elseeker.member.application.service.AdminMemberService
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
class AdminMemberWebController(
    private val adminMemberService: AdminMemberService,
) {

    @GetMapping("/members")
    fun memberList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) keyword: String?,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        val result = adminMemberService.findAll(keyword, pageable)
        model.addAttribute("page", result)
        model.addAttribute("keyword", keyword.orEmpty())
        return "admin/admin-member-list"
    }

    @GetMapping("/members/{id}/edit")
    fun memberEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("member", adminMemberService.findByIdWithOAuthAccounts(id))
        return "admin/admin-member-form"
    }
}
