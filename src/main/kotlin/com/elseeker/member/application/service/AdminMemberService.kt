package com.elseeker.member.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleChapterMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleHighlightRepository
import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
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
import com.elseeker.member.domain.vo.MemberRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminMemberService(
    private val memberRepository: MemberRepository,
    private val memberOAuthAccountRepository: MemberOAuthAccountRepository,
    private val bibleMemoRepository: BibleMemoRepository,
    private val bibleChapterMemoRepository: BibleChapterMemoRepository,
    private val bibleBookMemoRepository: BibleBookMemoRepository,
    private val bibleHighlightRepository: BibleHighlightRepository,
    private val bibleTypingSessionRepository: BibleTypingSessionRepository,
    private val quizProgressRepository: QuizProgressRepository,
    private val quizStageAttemptRepository: QuizStageAttemptRepository,
    private val quizQuestionAttemptRepository: QuizQuestionAttemptRepository,
    private val quizStageProgressRepository: QuizStageProgressRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository,
    private val memberWithdrawalAuditRepository: MemberWithdrawalAuditRepository,
) {
    fun findAll(keyword: String?, pageable: Pageable): Page<Member> =
        memberRepository.searchByKeyword(keyword, pageable)

    fun findById(id: Long): Member =
        memberRepository.findByIdOrNull(id) ?: throwError(ErrorType.MEMBER_NOT_FOUND, "id=$id")

    fun findByIdWithOAuthAccounts(id: Long): Member =
        memberRepository.findWithOAuthAccountsById(id) ?: throwError(ErrorType.MEMBER_NOT_FOUND, "id=$id")

    @Transactional
    fun update(id: Long, nickname: String, profileImageUrl: String?, memberRole: MemberRole): Member {
        val member = findById(id)
        if (memberRepository.existsByNicknameIgnoreCaseAndIdNot(nickname.trim(), id)) {
            throwError(ErrorType.NICKNAME_ALREADY_EXISTS)
        }
        member.update(nickname, profileImageUrl)
        member.memberRole = memberRole
        return memberRepository.save(member)
    }

    @Transactional
    fun delete(id: Long) {
        val member = findById(id)
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
        bibleTypingSessionRepository.deleteAllByMember(member)
        quizQuestionAttemptRepository.deleteAllByMember(member)
        quizStageAttemptRepository.deleteAllByMember(member)
        quizQuestionStatRepository.deleteAllByMember(member)
        quizStageProgressRepository.deleteAllByMember(member)
        quizProgressRepository.deleteAllByMember(member)
        memberOAuthAccountRepository.deleteAllByMember(member)
        memberRepository.delete(member)
    }
}
