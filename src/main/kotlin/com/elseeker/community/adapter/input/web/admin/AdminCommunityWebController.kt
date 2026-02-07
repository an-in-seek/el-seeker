package com.elseeker.community.adapter.input.web.admin

import com.elseeker.community.application.mapper.toAdminItem
import com.elseeker.community.application.service.AdminCommunityReportService
import com.elseeker.community.application.service.CommentService
import com.elseeker.community.application.service.PostService
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.TargetType
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/web/admin/community")
class AdminCommunityWebController(
    private val postService: PostService,
    private val commentService: CommentService,
    private val adminCommunityReportService: AdminCommunityReportService,
) {

    @GetMapping
    fun communityRoot(): String = "redirect:/web/admin/community/posts"

    @GetMapping("/posts")
    fun postList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) type: PostType?,
        @RequestParam(required = false) status: PostStatus?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) author: String?,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        val result = postService.getAdminPosts(type, status, keyword, author, pageable)
        model.addAttribute("page", result)
        model.addAttribute("type", type)
        model.addAttribute("status", status)
        model.addAttribute("keyword", keyword.orEmpty())
        model.addAttribute("author", author.orEmpty())
        return "admin/admin-community-post-list"
    }

    @GetMapping("/posts/new")
    fun postNewForm(): String = "admin/admin-community-post-form"

    @GetMapping("/posts/{postId}/edit")
    fun postEditForm(@PathVariable postId: Long, model: Model): String {
        model.addAttribute("post", postService.getAdminPostDetail(postId))
        return "admin/admin-community-post-form"
    }

    @GetMapping("/posts/{postId}")
    fun postDetail(@PathVariable postId: Long, model: Model): String {
        model.addAttribute("post", postService.getAdminPostDetail(postId))
        return "admin/admin-community-post-detail"
    }

    @GetMapping("/comments")
    fun commentList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: CommentStatus?,
        @RequestParam(required = false) postId: Long?,
        @RequestParam(required = false) commentId: Long?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) author: String?,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        val result = commentService.getAdminComments(status, postId, commentId, keyword, author, pageable)
        val mapped = PageImpl(result.content.map { it.toAdminItem() }, pageable, result.totalElements)
        model.addAttribute("page", mapped)
        model.addAttribute("status", status)
        model.addAttribute("postId", postId)
        model.addAttribute("commentId", commentId)
        model.addAttribute("keyword", keyword.orEmpty())
        model.addAttribute("author", author.orEmpty())
        return "admin/admin-community-comment-list"
    }

    @GetMapping("/reports")
    fun reportList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) targetType: TargetType?,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        val result = adminCommunityReportService.getAdminReports(targetType, pageable)
        model.addAttribute("page", result)
        model.addAttribute("targetType", targetType)
        return "admin/admin-community-report-list"
    }
}
