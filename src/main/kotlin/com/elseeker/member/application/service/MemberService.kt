package com.elseeker.member.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleChapterMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleHighlightRepository
import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleReadingProgressRepository
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.adapter.output.jpa.QuizProgressRepository
import com.elseeker.game.adapter.output.jpa.QuizQuestionAttemptRepository
import com.elseeker.game.adapter.output.jpa.QuizQuestionStatRepository
import com.elseeker.game.adapter.output.jpa.QuizStageAttemptRepository
import com.elseeker.game.adapter.output.jpa.QuizStageProgressRepository
import com.elseeker.member.adapter.output.jpa.MemberOAuthAccountRepository
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.adapter.output.jpa.MemberWithdrawalAuditRepository
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.model.MemberWithdrawalAudit
import com.elseeker.member.domain.vo.OAuthProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberOAuthAccountRepository: MemberOAuthAccountRepository,
    private val bibleMemoRepository: BibleMemoRepository,
    private val bibleChapterMemoRepository: BibleChapterMemoRepository,
    private val bibleBookMemoRepository: BibleBookMemoRepository,
    private val bibleHighlightRepository: BibleHighlightRepository,
    private val bibleReadingProgressRepository: BibleReadingProgressRepository,
    private val bibleTypingSessionRepository: BibleTypingSessionRepository,
    private val quizProgressRepository: QuizProgressRepository,
    private val quizStageAttemptRepository: QuizStageAttemptRepository,
    private val quizQuestionAttemptRepository: QuizQuestionAttemptRepository,
    private val quizStageProgressRepository: QuizStageProgressRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository,
    private val memberWithdrawalAuditRepository: MemberWithdrawalAuditRepository,
) {

    // TODO: 회원(Member) 가입

    // TODO: 회원(Member) 정보 조회

    // TODO: 회원(Member) 정보 수정

    @Transactional(readOnly = true)
    fun getMember(memberUid: UUID) = memberRepository.findByUid(memberUid)
        ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)

    @Transactional(readOnly = true)
    fun getMemberWithOAuthAccounts(memberUid: UUID) = memberRepository.findWithOAuthAccountsByUid(memberUid)
        ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)

    @Transactional
    fun deleteMember(memberUid: UUID, principalUid: UUID) {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        val member = getMember(memberUid)
        memberWithdrawalAuditRepository.save(
            MemberWithdrawalAudit(
                memberUid = member.uid,
                email = member.email,
                nickname = member.nickname
            )
        )
        bibleMemoRepository.deleteAllByMember(member)
        bibleChapterMemoRepository.deleteAllByMember(member)
        bibleBookMemoRepository.deleteAllByMember(member)
        bibleHighlightRepository.deleteAllByMember(member)
        bibleReadingProgressRepository.deleteAllByMember(member)
        bibleTypingSessionRepository.deleteAllByMember(member)
        quizQuestionAttemptRepository.deleteAllByMember(member)
        quizStageAttemptRepository.deleteAllByMember(member)
        quizQuestionStatRepository.deleteAllByMember(member)
        quizStageProgressRepository.deleteAllByMember(member)
        quizProgressRepository.deleteAllByMember(member)
        memberOAuthAccountRepository.deleteAllByMember(member)
        memberRepository.delete(member)
    }

    @Transactional
    fun updateMember(memberUid: UUID, principalUid: UUID, nickname: String, profileImageUrl: String?): Member {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        val member = getMember(memberUid)
        val normalizedNickname = nickname.trim()
        val memberId = member.id ?: throwError(ErrorType.MEMBER_ID_MISSING, memberUid)
        if (memberRepository.existsByNicknameIgnoreCaseAndIdNot(normalizedNickname, memberId)) {
            throwError(ErrorType.NICKNAME_ALREADY_EXISTS)
        }
        member.update(nickname, profileImageUrl)
        memberRepository.save(member)
        return memberRepository.findWithOAuthAccountsByUid(memberUid)
            ?: member
    }

    @Transactional
    fun linkOAuthAccount(
        memberUid: UUID,
        principalUid: UUID,
        providerRegistrationId: String,
        providerUserId: String
    ): Member {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        if (providerRegistrationId.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "provider")
        }
        if (providerUserId.isBlank()) {
            throwError(ErrorType.OAUTH_PROVIDER_USER_ID_MISSING, providerRegistrationId)
        }
        val provider = runCatching { OAuthProvider.fromRegistrationId(providerRegistrationId) }
            .getOrElse { throwError(ErrorType.INVALID_PARAMETER, providerRegistrationId) }
        val member = getMember(memberUid)
        val existingAccount = memberOAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
        if (existingAccount != null) {
            if (existingAccount.member.id == member.id) {
                return member
            }
            throwError(ErrorType.OAUTH_ACCOUNT_ALREADY_LINKED, provider.registrationId)
        }
        member.addOAuthAccount(provider, providerUserId)
        return memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    fun getOAuthAccounts(memberUid: UUID, principalUid: UUID) =
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        } else {
            memberOAuthAccountRepository.findAllByMemberUid(memberUid)
        }

    @Transactional
    fun unlinkOAuthAccount(
        memberUid: UUID,
        principalUid: UUID,
        providerRegistrationId: String,
        providerUserId: String
    ): Member {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        if (providerRegistrationId.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "provider")
        }
        if (providerUserId.isBlank()) {
            throwError(ErrorType.OAUTH_PROVIDER_USER_ID_MISSING, providerRegistrationId)
        }
        val provider = runCatching { OAuthProvider.fromRegistrationId(providerRegistrationId) }
            .getOrElse { throwError(ErrorType.INVALID_PARAMETER, providerRegistrationId) }
        val member = getMember(memberUid)
        val account = memberOAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            ?: throwError(ErrorType.OAUTH_ACCOUNT_NOT_FOUND, provider.registrationId)
        if (account.member.id != member.id) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        member.removeOAuthAccount(account)
        return memberRepository.save(member)
    }

    @Transactional
    fun initializeProfileFromOAuthAccount(
        memberUid: UUID,
        principalUid: UUID,
        providerRegistrationId: String,
        providerUserId: String
    ): Member {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        if (providerRegistrationId.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "provider")
        }
        if (providerUserId.isBlank()) {
            throwError(ErrorType.OAUTH_PROVIDER_USER_ID_MISSING, providerRegistrationId)
        }
        val provider = runCatching { OAuthProvider.fromRegistrationId(providerRegistrationId) }
            .getOrElse { throwError(ErrorType.INVALID_PARAMETER, providerRegistrationId) }
        val member = getMember(memberUid)
        val account = memberOAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            ?: throwError(ErrorType.OAUTH_ACCOUNT_NOT_FOUND, provider.registrationId)
        if (account.member.id != member.id) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        member.initializeProfileFromOAuth(account.nickname, account.profileImageUrl)
        return memberRepository.save(member)
    }

}
