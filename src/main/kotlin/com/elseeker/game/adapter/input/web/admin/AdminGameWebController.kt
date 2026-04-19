package com.elseeker.game.adapter.input.web.admin

import com.elseeker.game.application.service.AdminWordPuzzleService
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
class AdminGameWebController(
    private val adminWordPuzzleService: AdminWordPuzzleService,
) {

    @GetMapping("/word-puzzles")
    fun puzzleList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        model.addAttribute("page", adminWordPuzzleService.findAllPuzzles(pageable))
        return "admin/game/admin-word-puzzle-list"
    }

    @GetMapping("/word-puzzles/new")
    fun puzzleNewForm(): String = "admin/game/admin-word-puzzle-form"

    @GetMapping("/word-puzzles/{id}/edit")
    fun puzzleEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("puzzle", adminWordPuzzleService.findPuzzleById(id))
        return "admin/game/admin-word-puzzle-form"
    }

    @GetMapping("/word-puzzles/{puzzleId}/entries")
    fun entryList(
        @PathVariable puzzleId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        model: Model,
    ): String {
        model.addAttribute("puzzle", adminWordPuzzleService.findPuzzleById(puzzleId))
        val pageable = PageRequest.of(page, size)
        model.addAttribute("page", adminWordPuzzleService.findAllEntries(puzzleId, pageable))
        return "admin/game/admin-word-puzzle-entry-list"
    }

    @GetMapping("/word-puzzles/{puzzleId}/entries/new")
    fun entryNewForm(@PathVariable puzzleId: Long, model: Model): String {
        model.addAttribute("puzzleId", puzzleId)
        return "admin/game/admin-word-puzzle-entry-form"
    }

    @GetMapping("/word-puzzles/{puzzleId}/entries/{entryId}/edit")
    fun entryEditForm(@PathVariable puzzleId: Long, @PathVariable entryId: Long, model: Model): String {
        model.addAttribute("puzzleId", puzzleId)
        model.addAttribute("entry", adminWordPuzzleService.findEntryById(puzzleId, entryId))
        return "admin/game/admin-word-puzzle-entry-form"
    }
}
