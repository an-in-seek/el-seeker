package com.elseeker.member.adapter.input.api.admin

import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.member.adapter.input.api.admin.request.AdminMemberUpdateRequest
import com.elseeker.member.application.service.AdminMemberService
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.model.MemberOAuthAccount
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/members")
class AdminMemberApi(
    private val adminMemberService: AdminMemberService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) keyword: String?,
        pageable: Pageable
    ): ResponseEntity<AdminPageResponse<MemberItem>> {
        val effectivePageable = if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "id"))
        }
        val result = adminMemberService.findAll(keyword, effectivePageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { MemberItem.from(it) })
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long): ResponseEntity<MemberDetail> =
        ResponseEntity.ok(MemberDetail.from(adminMemberService.findByIdWithOAuthAccounts(id)))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: AdminMemberUpdateRequest
    ): ResponseEntity<MemberItem> {
        val updated = adminMemberService.update(id, request.nickname, request.profileImageUrl, request.memberRole)
        return ResponseEntity.ok(MemberItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminMemberService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class MemberItem(
        val id: Long,
        val uid: String,
        val email: String,
        val nickname: String,
        val profileImageUrl: String?,
        val memberRole: String,
        val createdAt: String,
        val updatedAt: String,
    ) {
        companion object {
            fun from(member: Member) = MemberItem(
                id = member.id ?: 0L,
                uid = member.uid.toString(),
                email = member.email,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl,
                memberRole = member.memberRole.name,
                createdAt = member.createdAt.toString(),
                updatedAt = member.updatedAt.toString(),
            )
        }
    }

    data class MemberDetail(
        val id: Long,
        val uid: String,
        val email: String,
        val nickname: String,
        val profileImageUrl: String?,
        val memberRole: String,
        val createdAt: String,
        val updatedAt: String,
        val oauthAccounts: List<OAuthAccountItem>,
    ) {
        companion object {
            fun from(member: Member) = MemberDetail(
                id = member.id ?: 0L,
                uid = member.uid.toString(),
                email = member.email,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl,
                memberRole = member.memberRole.name,
                createdAt = member.createdAt.toString(),
                updatedAt = member.updatedAt.toString(),
                oauthAccounts = member.oauthAccounts
                    .sortedBy { it.provider.name }
                    .map(OAuthAccountItem::from)
            )
        }
    }

    data class OAuthAccountItem(
        val provider: String,
        val providerUserId: String,
        val email: String?,
        val nickname: String?,
        val profileImageUrl: String?,
        val lastSyncedAt: String?
    ) {
        companion object {
            fun from(account: MemberOAuthAccount) = OAuthAccountItem(
                provider = account.provider.name,
                providerUserId = account.providerUserId,
                email = account.email,
                nickname = account.nickname,
                profileImageUrl = account.profileImageUrl,
                lastSyncedAt = account.lastSyncedAt?.toString()
            )
        }
    }
}
