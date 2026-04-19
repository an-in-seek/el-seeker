package com.elseeker.bible.adapter.input.web.admin

import com.elseeker.bible.application.service.AdminBibleBookDescriptionService
import com.elseeker.bible.application.service.AdminBibleBookService
import com.elseeker.bible.application.service.AdminBibleChapterService
import com.elseeker.bible.application.service.AdminBibleTranslationService
import com.elseeker.bible.application.service.AdminBibleVerseService
import com.elseeker.bible.domain.vo.BibleBookKey
import com.neovisionaries.i18n.LanguageCode
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
class AdminBibleWebController(
    private val adminBibleTranslationService: AdminBibleTranslationService,
    private val adminBibleBookService: AdminBibleBookService,
    private val adminBibleBookDescriptionService: AdminBibleBookDescriptionService,
    private val adminBibleChapterService: AdminBibleChapterService,
    private val adminBibleVerseService: AdminBibleVerseService,
) {

    // ── 대시보드 ──

    @GetMapping
    fun dashboard(): String = "admin/admin-dashboard"

    // ── BibleTranslation ──

    @GetMapping("/bible/translations")
    fun translationList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("translationOrder"))
        model.addAttribute("page", adminBibleTranslationService.findAll(pageable))
        return "admin/bible/admin-bible-translation-list"
    }

    @GetMapping("/bible/translations/new")
    fun translationNewForm(): String = "admin/bible/admin-bible-translation-form"

    @GetMapping("/bible/translations/{id}/edit")
    fun translationEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("translation", adminBibleTranslationService.findById(id))
        return "admin/bible/admin-bible-translation-form"
    }

    // ── BibleBook ──

    @GetMapping("/bible/translations/{translationId}/books")
    fun bookList(
        @PathVariable translationId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("bookOrder"))
        model.addAttribute("page", adminBibleBookService.findByTranslationId(translationId, pageable))
        model.addAttribute("translationId", translationId)
        model.addAttribute("translation", adminBibleTranslationService.findById(translationId))
        return "admin/bible/admin-bible-book-list"
    }

    @GetMapping("/bible/translations/{translationId}/books/new")
    fun bookNewForm(@PathVariable translationId: Long, model: Model): String {
        model.addAttribute("translationId", translationId)
        return "admin/bible/admin-bible-book-form"
    }

    @GetMapping("/bible/translations/{translationId}/books/{id}/edit")
    fun bookEditForm(@PathVariable translationId: Long, @PathVariable id: Long, model: Model): String {
        model.addAttribute("translationId", translationId)
        model.addAttribute("book", adminBibleBookService.findById(id))
        return "admin/bible/admin-bible-book-form"
    }

    // ── BibleBookDescription ──

    @GetMapping("/bible/book-descriptions")
    fun bookDescriptionList(
        @RequestParam(required = false) bookKey: BibleBookKey?,
        @RequestParam(required = false) languageCode: LanguageCode?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("bookKey", "languageCode"))
        model.addAttribute("page", adminBibleBookDescriptionService.findAll(bookKey, languageCode, pageable))
        model.addAttribute("bookKey", bookKey)
        model.addAttribute("languageCode", languageCode)
        model.addAttribute("bookKeys", BibleBookKey.entries)
        return "admin/bible/admin-bible-book-description-list"
    }

    @GetMapping("/bible/book-descriptions/new")
    fun bookDescriptionNewForm(model: Model): String {
        model.addAttribute("bookKeys", BibleBookKey.entries)
        return "admin/bible/admin-bible-book-description-form"
    }

    @GetMapping("/bible/book-descriptions/{id}/edit")
    fun bookDescriptionEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("description", adminBibleBookDescriptionService.findById(id))
        model.addAttribute("bookKeys", BibleBookKey.entries)
        return "admin/bible/admin-bible-book-description-form"
    }

    // ── BibleChapter ──

    @GetMapping("/bible/books/{bookId}/chapters")
    fun chapterList(
        @PathVariable bookId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("chapterNumber"))
        model.addAttribute("page", adminBibleChapterService.findByBookId(bookId, pageable))
        model.addAttribute("bookId", bookId)
        model.addAttribute("book", adminBibleBookService.findById(bookId))
        return "admin/bible/admin-bible-chapter-list"
    }

    @GetMapping("/bible/books/{bookId}/chapters/new")
    fun chapterNewForm(@PathVariable bookId: Long, model: Model): String {
        model.addAttribute("bookId", bookId)
        return "admin/bible/admin-bible-chapter-form"
    }

    @GetMapping("/bible/books/{bookId}/chapters/{id}/edit")
    fun chapterEditForm(@PathVariable bookId: Long, @PathVariable id: Long, model: Model): String {
        model.addAttribute("bookId", bookId)
        model.addAttribute("chapter", adminBibleChapterService.findById(id))
        return "admin/bible/admin-bible-chapter-form"
    }

    // ── BibleVerse ──

    @GetMapping("/bible/chapters/{chapterId}/verses")
    fun verseList(
        @PathVariable chapterId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("verseNumber"))
        model.addAttribute("page", adminBibleVerseService.findByChapterId(chapterId, pageable))
        model.addAttribute("chapterId", chapterId)
        model.addAttribute("chapter", adminBibleChapterService.findById(chapterId))
        return "admin/bible/admin-bible-verse-list"
    }

    @GetMapping("/bible/chapters/{chapterId}/verses/new")
    fun verseNewForm(@PathVariable chapterId: Long, model: Model): String {
        model.addAttribute("chapterId", chapterId)
        return "admin/bible/admin-bible-verse-form"
    }

    @GetMapping("/bible/chapters/{chapterId}/verses/{id}/edit")
    fun verseEditForm(@PathVariable chapterId: Long, @PathVariable id: Long, model: Model): String {
        model.addAttribute("chapterId", chapterId)
        model.addAttribute("verse", adminBibleVerseService.findById(id))
        return "admin/bible/admin-bible-verse-form"
    }
}
